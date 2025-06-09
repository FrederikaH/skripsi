<?php

namespace App\Services;

use App\Models\Trip;
use App\Models\Route;
use App\Models\User;
use App\Models\UserTripStat;
use App\Models\UserRecommendation;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;
use Illuminate\Database\Eloquent\Builder;
use App\Http\Resources\RouteCollection;
use App\Http\Resources\RouteResource;

// ada kemungkinan user bisa lebih suka di kota lain / tempat baru yang jarang dikunjungi
class RecommendationRouteService
{
    function calculateAverage($totalValue, $totalTrip)
    {
        return $totalValue / $totalTrip;
    }

    // ranking multi city
    // cth: sby=3, jogja=2, jkt=0 -> diambil sby dan jogja dg poin masing2
    // normalisasi dan beri bobot
    function getMostFrequentValues($array, $property)
    {
        $counts = [];

        foreach ($array as $item) {
            if (isset($item->$property) && isset($item->count)) {
                $counts[$item->$property] = ($counts[$item->$property] ?? 0) + $item->count;
            }
        }

        return array_keys($counts, max($counts));
    }


    function getValuesCount($array, $property)
    {
        $counts = [];

        foreach ($array as $item) {
            if (isset($item->$property) && isset($item->count)) {
                $counts[$item->$property] = ($counts[$item->$property] ?? 0) + $item->count;
            }
        }

        return $counts;
    }

    function getPreferredRoadType($roadTypes, $cyclingTypes) {
        $cyclingTypePercentage = 0.1;
        $preferredRoadTypes = [];
    
        foreach ($roadTypes as $roadType) {
            $preferredRoadTypes[$roadType->road] = $roadType->count;
        }

        $cyclingTypeMapping = [
            'road' => 'paved_road',
            'gravel' => 'gravel_road',
            'mountain' => 'off_road'
        ];
    
        $totalRoadCount = array_sum($preferredRoadTypes);
        $additionalScore = $totalRoadCount * $cyclingTypePercentage;

        if (!empty($cyclingTypes)) {

            foreach ($cyclingTypes as $cyclingType) {
                if (isset($cyclingTypeMapping[$cyclingType])) {
                    $mappedRoadType = $cyclingTypeMapping[$cyclingType];
    
                    if (isset($preferredRoadTypes[$mappedRoadType])) {
                        $preferredRoadTypes[$mappedRoadType] += $additionalScore;
                    } else {
                        $preferredRoadTypes[$mappedRoadType] = $additionalScore;

                    }
                }
            }
        }
    
        return $preferredRoadTypes;
    }

    public function getPreferredUserData($user, $user_trip_stat)
    {
        $totalDistance = $user_trip_stat->sum('total_distance');
        $totalElevation = $user_trip_stat->sum('total_elevation_gain');
        $totalTrip = $user_trip_stat->sum('trip_count');

        $distanceAvg = $this->calculateAverage($totalDistance, $totalTrip);
        $elevationAvg = $this->calculateAverage($totalElevation, $totalTrip);

        $purposes = DB::table('routes as r')
            ->select('r.purpose', DB::raw('COUNT(r.purpose) as count'))
            ->join('trips as t', 'r.id', '=', 't.route_id')
            ->where('t.user_id', $user->id)
            ->whereNotNull('r.purpose')
            ->groupBy('r.purpose')
            ->get();

        $preferredPurposes = $this->getValuesCount($purposes, 'purpose');

        $preferredPurposes = array_change_key_case($preferredPurposes, CASE_LOWER);

        $locations = DB::table('routes as r')
        ->select('r.location_id', DB::raw('COUNT(r.location_id) as count'))
        ->join('trips as t', 'r.id', '=', 't.route_id')
        ->where('t.user_id', $user->id)
        ->whereNotNull('r.location_id')
        ->groupBy('r.location_id')
        ->get();

        // get just the list of visited IDs (e.g. [1, 2, 5])
        $preferredLocations = $locations
            ->pluck('location_id')
            ->unique()
            ->values()
            ->all();

        // $preferredLocations = $this->getValuesCount($locations, 'location_id');

        // // Get location_id counts
        // $locationCounts = $this->getValuesCount($locations, 'location_id');

        // // Only keep the keys (visited location IDs) as keys with boolean true for quick lookup
        // $preferredLocations = array_fill_keys(array_keys($locationCounts), true);

        $roadTypes = DB::table('routes as r')
            ->select('road_type.road', DB::raw('COUNT(road_type.road) as count'))
            ->join('trips as t', 'r.id', '=', 't.route_id')
            ->join(DB::raw("
                JSON_TABLE(
                    r.road_type, 
                    '$[*]' COLUMNS (
                        road VARCHAR(100) PATH '$'
                    )
                ) AS road_type
            "), function ($join) {
            })
            ->where('t.user_id', $user->id)
            ->whereNotNull('r.purpose')
            ->groupBy('road_type.road')
            ->get();
        
        $preferredRoadTypes = $this->getPreferredRoadType($roadTypes, $user['cycling_style']);

        $preferredData = [
            "distance" => $distanceAvg,
            "elevation" => $elevationAvg,
            "road_type" => $preferredRoadTypes,
            "purpose" => $preferredPurposes,
            "location_id" => $preferredLocations
        ];
        
        return $preferredData;
    }

    function getUnvisitedLocationIds($user)
    {
        $allLocationIds = DB::table('locations')->pluck('id');

        $visitedLocationIds = DB::table('routes as r')
            ->join('trips as t', 'r.id', '=', 't.route_id')
            ->where('t.user_id', $user->id)
            ->whereNotNull('r.location_id')
            ->pluck('r.location_id')
            ->unique();

        return $allLocationIds->diff($visitedLocationIds)->values();
    }

    function calculateNumericalScore($distances, $preferredDistance)
    {
        // hitung selisih absolut distance dgn preferred distance
        $distanceDiffs = array_map(fn($distance) => abs($distance - $preferredDistance), $distances);

        // cari min dan max nya
        $minDiff = min($distanceDiffs);
        $maxDiff = max($distanceDiffs);

        // jika min dan max sama, maka assign pointnya = 1
        if ($minDiff == $maxDiff) {
            return array_fill(0, count($distances), 1);
        }

        // lakukan normalisasi dengan rumus minmax, lalu -1 (karena selisihnya makin besar jadinya skor makin kecil, hrsnya sebaliknya)
        $distanceArr = array_map(fn($diff) => 1 - (($diff - $minDiff) / ($maxDiff - $minDiff)), $distanceDiffs);
        return $distanceArr;
    }

    function calculateMultiValueScore($array, $preferredValue)
    {
        if (empty($preferredValue)) {
            return array_fill(0, count($array), 0);
        }

        $minCount = 0;
        $maxCount = max($preferredValue);

        if ($minCount == $maxCount) {
            return array_fill(0, count($array), 1);
        }

        $valueScores = [];

        foreach ($array as $index => $object) {
            if (isset($preferredValue[$object])) {
                $score = ($preferredValue[$object] - $minCount) / ($maxCount - $minCount);
            } else {
                $score = 0;
            }

            $valueScores[$index] = $score;
        }

        return $valueScores;
    }

    function calculateRoadTypeScore($routes, $preferredRoadTypes)
    {
        if (empty($preferredRoadTypes)) {
            return array_fill(0, count($routes), 0);
        }

        $minCount = 0;
        $maxCount = max($preferredRoadTypes);

        if ($minCount == $maxCount) {
            $normalizedRoadTypes = array_fill_keys(array_keys($preferredRoadTypes), 1);
        } else {
            $normalizedRoadTypes = array_map(fn($count) => ($count - $minCount) / ($maxCount - $minCount), $preferredRoadTypes);
        }

        $tripScores = array_map(function ($route) use ($normalizedRoadTypes) {
            if (!is_array($route)) {
                return 0;
            }

            $scores = array_map(fn($road) => $normalizedRoadTypes[$road] ?? 0, $route);
            
            return !empty($scores) ? array_sum($scores) / count($scores) : 0;
        }, $routes);

        return $tripScores;
    }
    
    function calculateRouteScores($routes, $preferredUserData)
    {
        // Make sure $routes is a zero-indexed array so indexes match score arrays
        $routes = array_values($routes);

        $allDistances = array_column($routes, 'distance');
        $allElevations = array_column($routes, 'elevation');
        $allRoadTypes = array_column($routes, 'road_type');
        $allPurposes = array_column($routes, 'purpose');
        $allLocations = array_column($routes, 'location_id');

        $distanceScore = $this->calculateNumericalScore($allDistances, $preferredUserData['distance']);
        $elevationScore = $this->calculateNumericalScore($allElevations, $preferredUserData['elevation']);
        $roadTypeScore = $this->calculateRoadTypeScore($allRoadTypes, $preferredUserData['road_type']);
        $purposeScore = $this->calculateMultiValueScore($allPurposes, $preferredUserData['purpose']);
        
        $visitedLocationIds = array_keys($preferredUserData['location_id'] ?? []);
        $locationScore = array_map(function ($locationId) use ($visitedLocationIds) {
            return in_array($locationId, $visitedLocationIds) ? 0 : 1;
        }, $allLocations);

        // $locationScore = $this->calculateMultiValueScore($allLocations, $preferredUserData['location_id']);

        $scoredRoutes = array_map(function ($route, $index) use (
            $distanceScore, $elevationScore, $roadTypeScore, 
            $purposeScore, $locationScore, $preferredUserData
        ) {
            return [
                "id" => $route['id'],
                "name" => $route['name'],
                "distance" => $distanceScore[$index],
                "elevation" => $elevationScore[$index],
                "road_type" => $roadTypeScore[$index],
                "purpose" => $purposeScore[$index],
                "location_id" => $locationScore[$index],
                // "total_score" => $distanceScore[$index] + $elevationScore[$index] +  $roadTypeScore[$index] + $purposeScore[$index] + $locationScore[$index],
                "original" => $route,

                // Embed preferences for side-by-side display
                "preferred_data" => $preferredUserData,

                // Optional: count how many road_types matched
                "road_type_match_count" => count(array_intersect(
                    $route['road_type'], 
                    array_keys($preferredUserData['road_type'])
                )),
            ];
        }, $routes, array_keys($routes));

        return $scoredRoutes;
    }

    function rankRoutes($scoredRoutes, $weights)
    {
        $scoredRoutes = array_map(function ($route) use ($weights) {
            $route['weighted_score'] = array_sum(
                array_map(fn($key) => $route[$key] * $weights[$key], array_keys($weights))
            );
            return $route;
        }, $scoredRoutes);

        usort($scoredRoutes, fn($a, $b) =>
            $b['weighted_score'] <=> $a['weighted_score']
        );

        $topRanked = array_slice($scoredRoutes, 0, 10);

        // Ambil ID dari rute yang di-rank
        $rankedIds = array_map(fn($route) => $route['original']['id'], $topRanked);

        // Ambil data Route dari DB
        $routes = \App\Models\Route::whereIn('id', $rankedIds)->get();

        // Urutkan sesuai urutan skor (karena `whereIn` tidak menjamin urutan)
        $sortedRoutes = collect($rankedIds)->map(function ($id) use ($routes) {
            return $routes->firstWhere('id', $id);
        });

        // Kembalikan sebagai koleksi RouteResource
        // return RouteResource::collection($sortedRoutes);


        return array_values(array_map(fn($route) => [
            'id' => $route['original']['id'],
            'name' => $route['original']['name'],
            // 'start_location' => $route['original']['start_latitude'] . ', ' . $route['original']['start_longitude'],


            'location_id' => $route['original']['location_id'],
            
            // whether this location was visited before or not
            'visited_loc' => isset($route['preferred_data']['location_id'][$route['original']['location_id']]) ? 'yes' : 'no',

            // 'pref_loc_id'  => $route['preferred_data']['location_id'],
            'location_score' => round($route['location_id'], 3),

            'road_type' => $route['original']['road_type'],
            'pref_road_type' => $route['preferred_data']['road_type'],
            'road_type_score' => round($route['road_type'], 3),

            'distance' => $route['original']['distance'],
            'pref_distance' => round($route['preferred_data']['distance'], 2),
            'distance_score' => round($route['distance'], 3),

            'elevation' => $route['original']['elevation'],
            'pref_elevation' => round($route['preferred_data']['elevation'], 2),
            'elevation_score' => round($route['elevation'], 3),

            'purpose' => $route['original']['purpose'],
            'pref_purpose' => $route['preferred_data']['purpose'],
            'purpose_score' => round($route['purpose'], 3),

            // 'total_score' => round($route['total_score'], 4),
            'weighted_score' => round($route['weighted_score'], 4),
        ], array_slice($scoredRoutes, 0, 10)));

    }

    public function getRecommendedRoutes($routes, $user, $user_trip_stat)
    {
        $unvisitedLocationIds = $this->getUnvisitedLocationIds($user);

        $route = $routes->filter(function ($r) use ($unvisitedLocationIds) {
            return in_array($r['location_id'], $unvisitedLocationIds->toArray());
        });
        
        $weights = [
            "road_type" => 0.2519,
            "purpose" => 0.2469,
            "distance" => 0.1691,
            "location_id" => 0.1688,
            "elevation" => 0.1632
        ];

        if ($route->isEmpty() || !$user || $user_trip_stat->isEmpty()) {
            return response()->json([
                'message' => "You don't have enough trip"
            ], 200);
        }

        $preferredUserData = $this->getPreferredUserData($user, $user_trip_stat);

        $scoredRoutes = $this->calculateRouteScores($route->toArray(), $preferredUserData);
        $rankRoutes = $this->rankRoutes($scoredRoutes, $weights);
                
        $userId = $user->id;

        $newData = collect($rankRoutes)->map(fn($route) => [
            'user_id' => $userId,
            'route_id' => $route['id'],
            'score' => $route['weighted_score'],
        ]);
        
        $newRouteIds = $newData->pluck('route_id');
        
        UserRecommendation::where('user_id', $userId)
            ->whereNotIn('route_id', $newRouteIds)
            ->delete();
        
        $existingRouteIds = UserRecommendation::where('user_id', $userId)
            ->whereIn('route_id', $newRouteIds)
            ->pluck('route_id');
        
        $toInsert = $newData->reject(fn($rec) =>
            $existingRouteIds->contains($rec['route_id'])
        )->values()->all();
        
        UserRecommendation::insert($toInsert);  

        // $hitRateAtK = $this->calculateHitRateAtK($rankRoutes);
        $hitRateAtK = $this->calculatePrecisionAtK($rankRoutes);

        return $hitRateAtK;

        return response()->json([
            'data' => $rankRoutes
        ]);
    }

    public function calculatePrecisionAtK($routes, $k = 10, $threshold = 0.7)
    {
        // Make sure $routes is a collection
        $routesCollection = collect($routes);

        // Sort by total_score (or weighted_score, depending on what your data contains)
        $sortedRoutes = $routesCollection->sortByDesc(function ($route) {
            return $route['total_score'] ?? $route['weighted_score'] ?? 0;
        })->values();

        // Take top K
        $topKRoutes = $sortedRoutes->take($k);

        // Count how many of top K are relevant
        $relevantCount = $topKRoutes->filter(function ($route) use ($threshold) {
            $score = $route['total_score'] ?? $route['weighted_score'] ?? 0;
            return $score >= $threshold;
        })->count();

        // Calculate Precision@K
        $precisionAtK = $k > 0 ? $relevantCount / $k : 0;

        return round($precisionAtK, 4);
    }

    public function calculateHitRateAtK($routes, $k = 10, $threshold = 0.7)
    {
        // If the input is a Laravel Resource, convert to array
        $request = request();
        $routesArray = collect($routes)->map(function ($route) use ($request) {
            return is_object($route) && method_exists($route, 'toArray')
                ? $route->toArray($request)
                : (array) $route;
        });

        // Sort by total_score descending
        $sortedRoutes = $routesArray->sortByDesc('total_score')->values();

        // Take top K
        $topKRoutes = $sortedRoutes->take($k);

        // Check if at least one is relevant
        $hasHit = $topKRoutes->contains(function ($route) use ($threshold) {
            $score = $route['weighted_score'] ?? 0;
            return $score >= $threshold;
        });

        return $hasHit ? 1 : 0;
    }

    // public function calculateMRR(array $routes, float $threshold = 0.8): float
    // {
    //     // Assume routes are already sorted by predicted score (desc)
    //     foreach ($routes as $index => $route) {
    //         $score = $route['weighted_score'] ?? 0;

    //         // If score above threshold, consider it relevant
    //         if ($score >= $threshold) {
    //             // Return Reciprocal Rank: 1-based index
    //             return round(1 / ($index + 1), 4);
    //         }
    //     }

    //     // No relevant item found
    //     return 0.0;
    // }
}