<?php

namespace App\Http\Controllers\API;

use App\Enum\TripStatisticPeriodEnum;
use App\Http\Controllers\Controller;
use App\Http\Requests\StoreTripRequest;
use App\Http\Resources\TripResource;
use App\Services\RecommendationRouteService;
use App\Models\File;
use App\Models\Route;
use App\Models\Trip;
use App\Models\UserRoute;
use App\Models\User;
use App\Models\TripEvent;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Illuminate\Support\Facades\DB;
use Illuminate\Database\Eloquent\Builder;
use App\Http\Resources\TripCollection;
use App\Jobs\UserTripStatCalculationJob;
use App\Models\Event;
use Illuminate\Container\Attributes\CurrentUser;
use Carbon\Carbon;
use App\UseCase\TripStatistic;
use App\Http\Resources\UserTripStatResource;
use App\Http\Resources\CommentResource;
use App\Jobs\UserGroupStatCalculationJob;
use App\Jobs\CalculateRecommendationRouteJob;
use App\Jobs\TripCalculationJob;
use App\Models\Comment;

class TripController extends Controller
{
    /**
     * Store a newly created trip in storage.
     *
     * @param StoreTripRequest $request The request containing trip data.
     * @param User $user The authenticated user.
     * @return \App\Http\Resources\TripResource
     */
    public function store(StoreTripRequest $request, #[CurrentUser] User $user)
    {
        // dd($request);
        $logMessage = 'TripController | store';
        $logContext = ['request' => $request->all()];

        info($logMessage, $logContext);

        return DB::transaction(callback: function () use ($request, $user, $logMessage, $logContext) {
            
            $thumbnailFileId = null;

            $images = $request->file('images');
    
            for ($i=0; $i < count($images); $i++) { 
                $randomName = "$user->id-" . Str::uuid() . '.' . $images[$i]->getClientOriginalExtension();
    
                $path = $images[$i]->storeAs('images', $randomName, 's3');
    
                $file = new File([
                    'type' => 'image',
                    'url' => $path
                ]);
    
                $file->save();
    
                if ($i == 0) {
                    $thumbnailFileId = $file->id;
                }
            }
    
            $file = $request->file('fit');
    
            $path = $file->storeAs('fit', $file->hashName(), 's3');
    
            $fitFile = new File([
                'type' => 'fit',
                'url' => $path
            ]);
    
            $fitFile->save();
    
            $file = $request->file('gpx');
    
            $path = $file->storeAs('gpx', $file->hashName(), 's3');
    
            $gpxFile = new File([
                'type' => 'gpx',
                'url' => $path
            ]);
    
            $gpxFile->save();
    
            $route = new Route([
                'name' => $request->name,
                'location_id' => $request->location_id ?: 1,
                'start_latitude' => $request->start_latitude,
                'start_longitude' => $request->start_longitude,
                'road_type' => $request->road_type,
                'distance' => $request->distance,
                'elevation' => $request->elevation,
                'purpose' => $request->purpose,
                'polyline' => $request->polyline,
                'gpx_file_id' => $gpxFile->id,
                'thumbnail_file_id' => $thumbnailFileId
            ]);
    
            $route->save();
    
            $userRoute = new UserRoute([
                'user_id' => $user->id,
                'route_id' => $route->id
            ]);
    
            $userRoute->save();
    
            $trip = new Trip([
                'timestamp' => $request->timestamp,
                'user_id' => $user->id,
                'name' => $request->name,
                'moving_time' => $request->moving_time,
                'elapsed_time' => $request->elapsed_time,
                'avg_speed' => $request->avg_speed,
                'max_speed' => $request->max_speed,
                'bike_name' => $request->bike_name,
                'route_id' => $route->id,
                'fit_file_id' => $fitFile->id,
                'is_private' => ($request->is_private) ?: false
            ]);
    
            $trip->save();

            UserTripStatCalculationJob::dispatch($route, $trip, $user)->afterCommit();
            UserGroupStatCalculationJob::dispatch($route, $trip, $user)->afterCommit();
            CalculateRecommendationRouteJob::dispatch($user)->afterCommit();
            TripCalculationJob::dispatch($trip->id)->afterCommit();
    
            $activeEvents = $user->followedPublicEvents()->active()->get();
    
            if ($activeEvents->isNotEmpty()) {
                $tripEventInserts = $activeEvents
                    ->filter(function (Event $event) {
                        return $event->route != null;
                    })
                    ->filter(function (Event $event) use ($request) {
                        $route = $event->route;
    
                        $distance = $this->getDistance(
                            latitude1: $route->start_latitude,
                            longitude1: $route->start_longitude,
                            latitude2: $request->start_latitude,
                            longitude2: $request->start_longitude
                        ) * 1000.0;
    
                        return $distance <= 200;
                    })
                    ->map(
                        fn ($event) => [
                            'trip_id' => $trip->id,
                            'event_id' => $event->id
                        ]
                    )
                    ->toArray();
    
                TripEvent::insert($tripEventInserts);
            }
    
            return new TripResource($trip);

        }, attempts: 1);
    }

    /**
     * Retrieve a list of trips, with optional filtering and embedding.
     *
     * @param Request $request The request containing query parameters like 'period', 'embed', 'user_id', 'limit'.
     * @param User $currentUser The authenticated user.
     * @return \App\Http\Resources\TripCollection
     */
    public function get(Request $request, #[CurrentUser] User $currentUser)
    {
        $period = $request->get('period');

        $query = Trip::when($period == 'month', function (Builder $query) {
            $query->whereMonth('created_at', Carbon::now()->month);
        })
        ->when($period == 'week', function (Builder $query) {
            $query->where(DB::raw('YEARWEEK(`created_at`, 1)'), '=', Carbon::now()->isoFormat('GGGGWW'));
        });

        $request->whenHas(
            'embed',
            function (string $input) use ($query) {
                $relations = explode(
                    separator: ",",
                    string: $input
                );

                $query->with($relations);
            }
        );

        if ($request->has('user_id')) {
            $query->where('user_id', $request->query("user_id"));
        } else {
            $query->whereNotLike('user_id', $currentUser->id);
        }

        $query->orderBy('timestamp', 'desc');

        if ($request->filled('limit')) {
            return new TripCollection($query->limit($request->get('limit'))->get());
        } else {
            return new TripCollection($query->simplePaginate(10));
        }
    }

    /**
     * Retrieve details of a specific trip by ID.
     *
     * @param Request $request The request containing the trip ID and optional 'embed' parameter.
     * @return \App\Http\Resources\TripResource
     */
    public function getDetail(Request $request)
    {
        $id = $request->id;

        $query = Trip::when('user_id')->where('id', $id);

        $request->whenHas(
            'embed',
            function (string $input) use ($query) {
                $relations = explode(
                    separator: ",",
                    string: $input
                );

                $query->with($relations);
            }
        );

        return new TripResource($query->first());
    }

    /**
     * Retrieve details of a specific trip using route model binding.
     *
     * @param Request $request The request containing optional 'embed' parameter.
     * @param Trip $trip The Trip model instance resolved via route model binding.
     * @return \App\Http\Resources\TripResource
     */
    public function detail(Request $request, Trip $trip)
    {
        $query = Trip::where('id', $trip->id);

        $request->whenHas(
            'embed',
            function (string $input) use ($query) {
                $relations = explode(
                    separator: ",",
                    string: $input
                );

                $query->with($relations);
            }
        );

        return new TripResource($query->first());
    }

    /**
     * Retrieve trip statistics summary for a user.
     *
     * @param Request $request The request containing 'period' and 'user_id' parameters.
     * @param \App\UseCases\TripStatistic $tripStatUseCase The trip statistic use case.
     * @param User $currentUser The authenticated user.
     * @return \App\Http\Resources\UserTripStatResource
     */
    public function summary(Request $request, TripStatistic $tripStatUseCase, #[CurrentUser] User $currentUser)
    {
        $period = $request->input(
            key: 'period',
            default: TripStatisticPeriodEnum::WEEK
        );

        $userId = $request->input(
            key: 'user_id',
            default: $currentUser->id
        );

        $periodKey = $tripStatUseCase->getPeriodKey($period);

        $statistic = $tripStatUseCase->getStatistic($periodKey, $userId);

        return new UserTripStatResource($statistic);
    }

    /**
     * Add a comment to a specific trip.
     *
     * @param Request $request The request containing the 'message'.
     * @param Trip $trip The Trip model instance to add the comment to.
     * @param User $user The authenticated user creating the comment.
     * @return \App\Http\Resources\CommentResource
     */
    public function addComment(Request $request, Trip $trip, #[CurrentUser] User $user)
    {
        $message = $request->input('message');

        $comment = new Comment([
            'user_id' => $user->id,
            'message' => $message
        ]);

        $trip->comments()->save($comment);

        return new CommentResource($comment);
    }

    /**
     * Calculates the distance between two geographical points using the Haversine formula.
     * Source: https://thisinterestsme.com/php-haversine-formula-function/
     *
     * @link https://thisinterestsme.com/php-haversine-formula-function/
     * @param float $latitude1 Latitude of the first point.
     * @param float $longitude1 Longitude of the first point.
     * @param float $latitude2 Latitude of the second point.
     * @param float $longitude2 Longitude of the second point.
     * @return float The distance in kilometers.
     */
    private function getDistance($latitude1, $longitude1, $latitude2, $longitude2) {
        $earth_radius = 6371;
    
        $dLat = deg2rad($latitude2 - $latitude1);
        $dLon = deg2rad($longitude2 - $longitude1);
    
        $a = sin($dLat/2) * sin($dLat/2) + cos(deg2rad($latitude1)) * cos(deg2rad($latitude2)) * sin($dLon/2) * sin($dLon/2);
        $c = 2 * asin(sqrt($a));
        $d = $earth_radius * $c;
    
        return $d;   
    }
}
