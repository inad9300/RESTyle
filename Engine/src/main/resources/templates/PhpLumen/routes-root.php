// Starting point for the API. It returns just a reference to itself, as a
// relative path, with the hope of being expanded to a wider set of helpful
// links specific to the particular problem in hands.
$app->get('/', function () use ($app) {
    return response()->json([
        '_links' => [
            'self' => [
                'href' => '/'
            ]
        ]
    ]);
});