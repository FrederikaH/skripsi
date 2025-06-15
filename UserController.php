<?php

namespace App\Http\Controllers\API;

use App\Http\Controllers\Controller;
use App\Http\Requests\FindUserRequest;
use App\Http\Requests\UpdateUserRequest;
use App\Http\Resources\TripCollection;
use App\Http\Resources\UserCollection;
use Illuminate\Http\Request;
use App\Http\Resources\UserResource;
use App\Models\User;
use App\Models\UserFollower;
use Illuminate\Container\Attributes\CurrentUser;
use \Illuminate\Support\Facades\DB;
use \App\Models\Trip;

class UserController extends Controller
{
    /**
     * Get the currently authenticated user's profile.
     *
     * @param Request $request
     * @param User $currentUser The authenticated user injected by Laravel.
     * @return UserResource
     */
    public function get(Request $request, #[CurrentUser] User $currentUser)
    {
        $query = User::query();

        $query->where('id', $currentUser->id);

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

        return new UserResource($query->first());
    }

    /**
     * Update the authenticated user's profile.
     *
     * @param UpdateUserRequest $request
     * @param User $currentUser The authenticated user injected by Laravel.
     * @return UserResource
     */
    public function update(UpdateUserRequest $request, #[CurrentUser] User $currentUser)
    {
        if ($request->filled('username')) {
            $currentUser->username = $request->username;
        }

        if ($request->filled('cycling_style')) {
            $currentUser->cycling_style = $request->cycling_style;
        }

        if ($request->filled('location_id')) {
            $currentUser->location_id = $request->location_id;
        }

        $currentUser->save();

        return new UserResource($currentUser);
    }

    /**
     * Delete the authenticated user's account.
     *
     * @param Request $request
     * @param User $currentUser The authenticated user injected by Laravel.
     * @return \Illuminate\Http\JsonResponse
     */
    public function delete(Request $request, #[CurrentUser] User $currentUser)
    {
        $currentUser->delete();

        return response()->json();
    }

    /**
     * Get the details of a specific user.
     *
     * @param Request $request
     * @param User $user The user model resolved from the route.
     * @return UserResource
     */
    public function detail(Request $request, User $user)
    {

        $query = User::query();

        $query->where('id', $user->id);

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

        return new UserResource($query->first());
    }

    /**
     * Follow a specific user.
     *
     * @param Request $request
     * @param User $user The user to be followed.
     * @param User $currentUser The authenticated user.
     * @return UserResource
     */
    public function follow(Request $request, User $user, #[CurrentUser] User $currentUser)
    {
        $relation = UserFollower::firstOrCreate([
            'user_id' => $user->id,
            'follower_user_id' => $currentUser->id
        ]);

        $relation->save();

        return new UserResource($user);
    }

    /**
     * Unfollow a specific user.
     *
     * @param Request $request
     * @param User $user The user to be unfollowed.
     * @param User $currentUser The authenticated user.
     * @return UserResource
     */
    public function unfollow(Request $request, User $user, #[CurrentUser] User $currentUser)
    {
        UserFollower::where('user_id', $user->id)
            ->where('follower_user_id', $currentUser->id)
            ->delete();

        return new UserResource($user);
    }

    /**
     * Find users based on provided criteria (e.g., cycling style, cities, name).
     *
     * @param FindUserRequest $request
     * @param User $currentUser The authenticated user (used to exclude self from results).
     * @return UserCollection
     */
    public function find(FindUserRequest $request, #[CurrentUser] User $currentUser)
    {
        $query = User::query();
        
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

        if ($request->filled('types')) {
            $types = explode(',', $request->query('types', ''));
            foreach ($types as $type) {
                $query->orWhereJsonContains('cycling_style', $type);
            }
        }

        if ($request->filled('cities')) {
            $cities = explode(',', $request->query('cities', ''));
            $query->whereIn('location_id', $cities);
        }

        if ($request->filled('name')) {
            $name = $request->name;

            $query->where('username', 'LIKE', "%$name%");
        }
        
        $query->where('id', '!=', $currentUser->id);

        return new UserCollection($query->get());
    }

    /**
     * Suggest a list of random users (excluding the current user).
     *
     * @param Request $request
     * @param User $currentUser The authenticated user.
     * @return UserCollection
     */
    public function suggest(Request $request, #[CurrentUser] User $currentUser)
    {
        $query = User::inRandomOrder();

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

        $query
            ->where('id', '!=', $currentUser->id)
            ->limit(5);

        return new UserCollection($query->get());
    }
}
