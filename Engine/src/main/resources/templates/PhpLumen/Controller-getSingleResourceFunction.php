    public function get{{subresourceClass}}(Request $req, $entityId) {
        if (Gate::denies('read', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to read the resource'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($entityId);
        $related = $entity->{{subFn}};

        self::addHalLink($related, 'self', $req->path());

        return response()->json($related);
    }