    public function getMany(Request $req) {
        if (Gate::denies('read', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to read the resource'
            ]);
        }

        $query = {{resourceClass}}::query();

        $query = self::addFieldsToQuery($query, $req);

        $query = self::addOrderByToQuery($query, $req, function ($prop) {
            return {{resourceClass}}::isSortable($prop);
        });

        $query = self::addLimitToQuery($query, $req);

        $query = self::addPageToQuery($query, $req);

        $query = self::addFilterToQuery($query, $req, function ($prop) {
            return {{resourceClass}}::isFilterable($prop);
        });

        $entities = $query->get();

        foreach ($entities as &$ent) {
            self::addHalLink($ent, 'self', $req->path() . '/' . $ent->id);
        }

        $result = new \stdClass;
        $result->count = count($entities);
        $result->total = {{resourceClass}}::count();

        self::addQueryMade($result, $req);
        self::addHalPageLinks($result, $req, $result->total);
        self::addHalEmbedded($result, '{{resourceNamePlural}}', $entities);

        return response()->json($result);
    }

    public function getOne(Request $req, $id) {
        if (Gate::denies('read', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to read the resource'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($id);

        self::addHalLink($entity, 'self', $req->path());

        return response()->json($entity);
    }

    public function create(Request $req) {
        if (Gate::denies('create', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to create the resource'
            ]);
        }

        $input = $req->all();

        if (Arrays::isAssoc($input)) {
            $this->validate($req, {{resourceClass}}::getValidationRules());
            $relatedEntities = {{resourceClass}}::create($input);
        } else if (is_array($input)) {
            foreach ($input as $relatedEntity) {
                $validator = Validator::make($relatedEntity, {{resourceClass}}::getValidationRules());
                if ($validator->fails()) {
                    return self::abort(422, ['invalid-fields' => $validator->messages()]);
                }
            }
            DB::transaction(function () {
                $relatedEntities = [];
                foreach ($input as $ent) {
                    $relatedEntities[] = {{resourceClass}}::create($ent);
                }
            });
        } else {
            return self::abort(HttpStatus::BadRequest, [
                'title' => 'Wrong data format',
                'detail' => 'Only JSON objects and JSON arrays are valid'
            ]);
        }

        return response()->json($relatedEntities);
    }

    public function fullUpdateOne(Request $req, $id) {
        if (Gate::denies('update', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to update the resource'
            ]);
        }

        $this->validate($req, {{resourceClass}}::getValidationRules());

        $entity = {{resourceClass}}::findOrFail($id);

        // If some property is not present in the input, null will be assigned
        foreach ($entity->getFillable() as $prop) {
            $entity[$prop] = $req->input($prop);
        }
        $entity->save();

        return response()->json($entity);
    }

    public function fullUpdateMany(Request $req) {
        if (Gate::denies('update', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to update the resource'
            ]);
        }

        $reqPets = $req->all();

        if (!is_array($reqPets)) {
            return self::abort(HttpStatus::BadRequest, [
                'title' => 'Wrong data format',
                'detail' => 'Only JSON objects and JSON arrays are valid'
            ]);
        }

        if (Arrays::isAssoc($reqPets)) {
            return $this->fullUpdateOne($req, $reqPets['{{resourceId}}']);
        }

        $entityIds = [];

        foreach ($reqPets as $ent) {
            $validator = Validator::make($ent, {{resourceClass}}::getValidationRules());
            if ($validator->fails()) {
                return self::abort(422, ['invalid-fields' => $validator->messages()]);
            }
            $entityIds[] = $entity['{{resourceId}}'];
        }

        $realPets = {{resourceClass}}::findOrFail($entityIds);

        DB::transaction(function () use ($realPets, $reqPets) {
            $numOfPets = count($realPets);
            for ($i = 0; $i < $numOfPets; $i++) {
                foreach ($realPets[$i]->getFillable() as $prop) {
                    $realPets[$i][$prop] = $reqPets[$i][$prop];
                }
                $realPets[$i]->save();
            }
        });

        return response()->json($realPets);
    }

    public function partialUpdateOne(Request $req, $id) {
        if (Gate::denies('update', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to update the resource'
            ]);
        }

        $this->validate($req, {{resourceClass}}::getValidationRules());

        $entity = {{resourceClass}}::findOrFail($id);

        $entity->fill($req->all());
        $entity->save();

        return response()->json($entity);
    }

    public function partialUpdateMany(Request $req) {
        if (Gate::denies('update', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to update the resource'
            ]);
        }

        $reqPets = $req->all();

        if (!is_array($reqPets)) {
            return self::abort(HttpStatus::BadRequest, [
                'title' => 'Wrong data format',
                'detail' => 'Only JSON objects and JSON arrays are valid'
            ]);
        }

        if (Arrays::isAssoc($reqPets)) {
            return $this->partialUpdateOne($req, $reqPets['{{resourceId}}']);
        }

        $entityIds = [];

        foreach ($reqPets as $entity) {
            $validator = Validator::make($entity, {{resourceClass}}::getValidationRules());
            if ($validator->fails()) {
                return self::abort(422, ['invalid-fields' => $validator->messages()]);
            }
            $entityIds[] = $entity['{{resourceId}}'];
        }

        $realPets = {{resourceClass}}::findOrFail($entityIds);

        DB::transaction(function () use ($realPets, $reqPets) {
            $numOfPets = count($realPets);
            for ($i = 0; $i < $numOfPets; $i++) {
                $realPets[$i]->fill($reqPets[$i]);
                $realPets[$i]->save();
            }
        });

        return response()->json($realPets);
    }

    public function deleteMany(Request $req) {
        if (Gate::denies('delete', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to delete the resource'
            ]);
        }

        if (!$req->getContent()) {
            $entities = {{resourceClass}}::all();
            {{resourceClass}}::truncate();
        } else if (is_array($req->all())) {
            $entities = {{resourceClass}}::findOrFail($req->all());
            {{resourceClass}}::destroy($req->all());
        } else {
            return self::abort(HttpStatus::BadRequest, [
                'title' => 'Wrong data format',
                'detail' => 'Either an empty body or a numeric array must be provided'
            ]);
        }

        return response()->json($entities);
    }

    public function deleteOne($id) {
        if (Gate::denies('delete', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to delete the resource'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($id);
        $entity->delete();

        return response()->json($entity);
    }