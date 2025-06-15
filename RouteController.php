<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Http\Controllers\API\TripController;
use App\Http\Requests\StoreRouteRequest;
use App\Http\Requests\UpdateRouteRequest;
use App\Http\Resources\CommentResource;
use App\Http\Resources\RouteCollection;
use App\Services\RecommendationRouteService;
use App\Services\GpxParserService;
use App\Http\Resources\RouteResource;
use App\Models\File;
use App\Models\Comment;
use App\Models\Route;
use App\Models\Trip;
use App\Models\UserTripStat;
use App\Models\User;
use App\Models\UserRecommendation;
use App\Models\UserRoute;
use Illuminate\Http\Request;
use Illuminate\Container\Attributes\CurrentUser;
use PHPUnit\Framework\Constraint\IsEmpty;
use Illuminate\Support\Facades\Auth;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\Log;


use function PHPUnit\Framework\isEmpty;

class RouteController extends Controller
{
    /**
     * Get a collection of routes based on various filters.
     *
     * @param Request $request The incoming HTTP request.
     * @param User $user The currently authenticated user.
     * @param RecommendationRouteService $recommendationRouteService The recommendation route service.
     * @return RouteCollection
     */
    public function get(Request $request, #[CurrentUser] User $user, RecommendationRouteService $recommendationRouteService)
    {
        $query = Route::query();
        
        $sortBy = 'desc';   
        $query->orderBy('created_at', $sortBy);
 
        if ($request->boolean('friends')) {
            $followingUserIds = $user->following()->pluck('users.id');

            $query->join('user_routes', 'routes.id', '=', 'user_routes.route_id')
                ->whereIn('user_routes.user_id', $followingUserIds)
                ->select('routes.*')
                ->orderBy('routes.id', 'desc');
        }

        if ($request->boolean('official')) {
            $query->where('user_generated', '0');
        }

        if ($request->filled('purpose')) {
            $purposes = explode(',', $request->query('purpose', ''));

            $query->where(function ($q) use ($purposes) {
                foreach ($purposes as $purpose) {
                    $q->orWhere('purpose', $purpose);
                }
            });
        }

        if ($request->filled('roads')) {
            $roads = explode(',', $request->query('roads', ''));
            $query->where(function ($q) use ($roads) {
                foreach ($roads as $road) {
                    $q->orWhereJsonContains('road_type', $road);
                }
            });
        }

        if (
            $request->filled('location.radius')
            && $request->filled('location.latitude')
            && $request->filled('location.longitude')
        ) {
            $query->nearby(
                $request->query('location')['latitude'],
                $request->query('location')['longitude'],
                $request->query('location')['radius']
            );
        }

        if ($request->filled('distance.from') && $request->filled('distance.to')) {
            $query->whereBetween('distance', [
                $request->input('distance.from'),
                $request->input('distance.to')
            ]);
        } elseif ($request->filled('distance.from')) {
            $query->where('distance', '>=', $request->input('distance.from'));
        } elseif ($request->filled('distance.to')) {
            $query->where('distance', '<=', $request->input('distance.to'));
        }

        if ($request->filled('elevation.from') && $request->filled('elevation.to')) {
            $query->whereBetween('elevation', [
                $request->input('elevation.from'),
                $request->input('elevation.to')
            ]);
        } elseif ($request->filled('elevation.from')) {
            $query->where('elevation', '>=', $request->input('elevation.from'));
        } elseif ($request->filled('elevation.to')) {
            $query->where('elevation', '<=', $request->input('elevation.to'));
        }

        if ($request->filled('cities')) {
            $cities = explode(',', $request->query('cities', ''));
            $query->whereIn('location_id', $cities);
        }

        if ($request->boolean('deleted')) {
            $query->onlyTrashed();
        }

        if ($request->boolean('official')) {
            $query->where('user_generated', '0');
        }

        if ($request->boolean('recommended')) {
            $recommendationRoutes = $user->recommendationRoutes()->get();
            return new RouteCollection($recommendationRoutes);
        } 
        
        $totalPerPage = 10;

        if ($request->filled('limit')) {
            $totalPerPage = $request->integer('limit');
        } 
        
        return new RouteCollection($query->simplePaginate($totalPerPage));
    }

    /**
     * Get the details of a specific route.
     *
     * @param Request $request The incoming HTTP request.
     * @param Route $route The route instance.
     * @return RouteResource
     */
    public function detail(Request $request, Route $route)
    {
        $query = Route::where('id', $route->id);

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

        return new RouteResource($query->first());
    }

    /**
     * Add a comment to a specific route.
     *
     * @param Request $request The incoming HTTP request containing the message.
     * @param Route $route The route to which the comment is added.
     * @param User $user The currently authenticated user.
     * @return CommentResource
     */
    public function addComment(Request $request, Route $route, #[CurrentUser] User $user)
    {
        $message = $request->input('message');

        $comment = new Comment([
            'user_id' => $user->id,
            'message' => $message
        ]);

        $route->comments()->save($comment);

        return new CommentResource($comment);
    }

    /**
     * Add a bookmark (clone) of a route for the current user.
     *
     * @param Request $request The incoming HTTP request, potentially containing a name for the bookmark.
     * @param Route $route The route to be bookmarked.
     * @param User $user The currently authenticated user.
     * @return RouteResource
     */
    public function addBookmark(Request $request, Route $route, #[CurrentUser] User $user)
    {
        $name = $request->name;

        $clone = $route->replicate();

        $clone->name = $name;

        $clone->user_generated = 1;

        $clone->save();

        $userRoute = new UserRoute([
            'user_id' => $user->id,
            'route_id' => $clone->id,
            'bookmark' => 1
        ]);

        $userRoute->save();

        return new RouteResource($clone);
    }

    /**
     * Get all bookmarked routes for the current user.
     *
     * @param Request $request The incoming HTTP request.
     * @param User $user The currently authenticated user.
     * @return RouteCollection
     */
    public function getBookmark(Request $request, #[CurrentUser] User $user)
    {
        return new RouteCollection($user->routes()->bookmarked()->get());
    }

    public function deleteRoute(Route $route)
    {
        if ($route->delete()) {
            return new RouteResource($route);
        }

        return response()->json([
            'message' => 'Failed to delete route.'
        ], 500);
    }

    public function restoreRoute(int $routeId)
    {
        $route = Route::withTrashed()->find($routeId);

        if (!$route) {
            return response()->json([
                'message' => 'Route not found.'
            ], 404);
        }

        if (!$route->trashed()) {
            return response()->json([
                'message' => 'Route is not soft deleted.'
            ], 400);
        }

        if ($route->restore()) {
            return new RouteResource($route);
        }

        return response()->json([
            'message' => 'Failed to restore route.'
        ], 500);
    }

    public function store(StoreRouteRequest $request)
    {
        $thumbnailFileId = null;

        // Upload images
        $imagesIds = collect($request->file('images'))->map(function (UploadedFile $image, int $key) use (&$thumbnailFileId) {
            $path = $image->storeAs('images', $image->hashName(), 's3');

            $file = File::create([
                'type' => 'image',
                'url' => $path,
            ]);

            if ($key === 0) {
                $thumbnailFileId = $file->id;
            }

            return $file->id;
        });

        // Upload GPX file
        $uploadedGpx = $request->file('file');
        if (!$uploadedGpx) {
            return response()->json(['error' => 'No GPX file uploaded'], 422);
        }

        $gpxPath = $uploadedGpx->storeAs('gpx', $uploadedGpx->hashName(), 's3');

        $gpxFile = File::create([
            'type' => 'gpx',
            'url' => $gpxPath,
        ]);

        // Parse GPX data
        $parsed = GpxParserService::extractRouteDataFromGpx($uploadedGpx->getRealPath());

        $startLat = $parsed['start_latitude'] ?? $request->start_latitude;
        $startLon = $parsed['start_longitude'] ?? $request->start_longitude;
        $distance = $parsed['distance'] ?? $request->distance;
        $elevation = isset($parsed['elevation']) ? (int)$parsed['elevation'] : (int)$request->elevation;

        // Save Route with GPX parsed data
        $route = Route::create([
            'name' => $request->name,
            'location_id' => $request->location_id,
            'start_latitude' => $startLat,
            'start_longitude' => $startLon,
            'distance' => $distance,
            'elevation' => $elevation,
            'polyline' => trim($request->polyline),
            'purpose' => $request->purpose,
            'road_type' => $request->road_type,
            'gpx_file_id' => $gpxFile->id,
            'thumbnail_file_id' => $thumbnailFileId,
            'user_generated' => 0,
        ]);

        $route->images()->attach($imagesIds);

        return new RouteResource($route);
    }

    public function update(UpdateRouteRequest $request, Route $route,  #[CurrentUser] User $currentUser)
    {
        $logMessage = 'RouteController | update';
        $logContext = ['request' => $request->all()];

        info($logMessage, $logContext);

        foreach (['name', 'location_id', 'start_latitude', 'start_longitude', 'distance', 'elevation', 'polyline', 'purpose', 'road_type'] as $field) {
            if ($request->filled($field)) {
                $route->$field = $request->$field;
            }
        }
        
        if ($request->hasFile('file')) {
            $uploadedGpx = $request->file('file');
            $gpxPath = $uploadedGpx->storeAs('gpx', $uploadedGpx->hashName(), 's3');

            $gpxFile = new File([
                'type' => 'gpx',
                'url' => $gpxPath
            ]);
            $gpxFile->save();

            $route->gpx_file_id = $gpxFile->id;

            $parsed = GpxParserService::extractRouteDataFromGpx($uploadedGpx->getRealPath());

            if ($parsed) {
                $route->start_latitude = $parsed['start_latitude'];
                $route->start_longitude = $parsed['start_longitude'];
                $route->distance = $parsed['distance'];
                $route->elevation = $parsed['elevation'];
            }
        }

        $oldImageIds = $request->input('old_image_ids', []);

        $route->load('images');
        $currentImages = $route->images;

        $imagesToDelete = $currentImages->filter(function ($image) use ($oldImageIds) {
            return !in_array($image->id, $oldImageIds);
        });

        foreach ($imagesToDelete as $file) {
            info('Deleting image', ['file_id' => $file->id, 'url' => $file->url]);
            Storage::disk('s3')->delete($file->url);
            $file->delete();
        }

        $newImageIds = [];

        if ($request->hasFile('images')) {
            $newImageIds = collect($request->file('images'))->map(function (UploadedFile $image, int $key) use (&$thumbnailFileId) {
                $path = $image->storeAs('images', $image->hashName(), 's3');

                $imageFile = new File([
                    'type' => 'image',
                    'url' => $path
                ]);
                $imageFile->save();

                return $imageFile->id;
            })->toArray();
        }

        $allImageIds = array_merge($oldImageIds, $newImageIds);
        $route->images()->sync($allImageIds);
        $route->thumbnail_file_id = $allImageIds[0] ?? $route->thumbnail_file_id;

        $route->save();

        $query = Route::where('id', $route->id);
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
        return new RouteResource($route);
    }

    public function getMostSavedRoutes()
    {
        $routes = Route::withCount([
            'users as bookmark_count' => function ($query) {
                $query
                ->where('bookmark', 1);
            }
        ])
        ->where('user_generated', '0')
        ->orderByDesc('bookmark_count');

        return new RouteCollection($routes->simplePaginate(10));
    }

    public function getRoadTypesCount(Request $request)
    {
        $period = $request->input('period', 'month');

        $startDate = match ($period) {
            'week' => Carbon::now()->startOfWeek(),
            'year' => Carbon::now()->startOfYear(),
            default => Carbon::now()->startOfMonth(),
        };

        $endDate = Carbon::now()->endOfDay();

        $results = DB::table('routes')
            ->selectRaw("jt.road_type, COUNT(*) as count")
            ->from(DB::raw("
                (
                    SELECT jt.road_type
                    FROM routes,
                    JSON_TABLE(
                        routes.road_type,
                        '$[*]' COLUMNS (road_type VARCHAR(50) PATH '$')
                    ) AS jt
                    WHERE routes.user_generated = 1
                    AND routes.created_at BETWEEN '{$startDate}' AND '{$endDate}'
                ) as jt
            "))
            ->groupBy('jt.road_type')
            ->get();

        $roadTypeCounts = [];
        foreach ($results as $row) {
            $key = str_replace([' ', '-'], '_', strtolower($row->road_type));
            $roadTypeCounts[$key] = $row->count;
        }

        return response()->json(['data' => $roadTypeCounts]);
    }
}