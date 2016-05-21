    public function get{{subresourceClass}}(Request $req, $entityId) {
        if (Gate::denies('read', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to read the resource'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($entityId);
        $related = $entity->{{subFn}};

        if (empty($related)) {
            throw new \Illuminate\Database\Eloquent\ModelNotFoundException();
        }

        self::addHalLink($related, 'self', $req->path());

        return response()->json($related);
    }