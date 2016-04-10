    public static function getMany(Request $req) {
        $query = DB::table('{{resourceTable}}');

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

        return response()->json($entities);
    }

    public static function getOne($id) {
        $entity = {{resourceClass}}::findOrFail($id);

        return response()->json($entity);
    }

    public static function create(Request $req) {
        $input = $req->all();

        if (Arrays::isAssoc($input)) {
            $this->validate($req, {{resourceClass}}::getValidationRules());
            $relatedEntities = {{resourceClass}}::create($input);
        } else if (is_array($input)) {
            foreach ($input as $relatedEntity) {
                $validator = Validator::make($relatedEntity, {{resourceClass}}::getValidationRules());
                if ($validator->fails()) {
                    return self::error($validator->messages());
                }
            }
            DB::transaction(function () {
                $relatedEntities = [];
                foreach ($input as $entity) {
                    $relatedEntities[] = {{resourceClass}}::create($entity);
                }
            });
        } else {
            // ...
        }

        return response()->json($relatedEntities);
    }

    public static function fullUpdateOne(Request $req, $id) {
        $this->validate($req, {{resourceClass}}::getValidationRules());

        $entity = {{resourceClass}}::findOrFail($id);

        // If some property is not present in the input, null will be assigned
        foreach ($entity->getFillable() as $prop) {
            $entity[$prop] = $req->input($prop);
        }
        $entity->save();

        return response()->json($entity);
    }

    public static function fullUpdateMany(Request $req) {
        $reqPets = $req->all();

        if (!is_array($reqPets)) {
            // ...
        }

        if (Arrays::isAssoc($reqPets)) {
            return $this->fullUpdateOne($req, $reqPets['{{resourceId}}']);
        }

        $entityIds = [];

        foreach ($reqPets as $entity) {
            $validator = Validator::make($entity, {{resourceClass}}::getValidationRules());
            if ($validator->fails()) {
                return self::error($validator->messages());
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

    public static function partialUpdateOne(Request $req, $id) {
        $this->validate($req, {{resourceClass}}::getValidationRules());

        $entity = {{resourceClass}}::findOrFail($id);

        $entity->fill($req->all());
        $entity->save();

        return response()->json($entity);
    }

    public static function partialUpdateMany(Request $req) {
        $reqPets = $req->all();

        if (!is_array($reqPets)) {
            // ...
        }

        if (Arrays::isAssoc($reqPets)) {
            return $this->partialUpdateOne($req, $reqPets['{{resourceId}}']);
        }

        $entityIds = [];

        foreach ($reqPets as $entity) {
            $validator = Validator::make($entity, {{resourceClass}}::getValidationRules());
            if ($validator->fails()) {
                return self::error($validator->messages());
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

    public static function deleteMany(Request $req) {
        if (!$req->getContent()) {
            $entities = {{resourceClass}}::all();
            {{resourceClass}}::truncate();
        } else if (is_array($req->all())) {
            $entities = {{resourceClass}}::findOrFail($req->all());
            {{resourceClass}}::destroy($req->all());
        } else {
            // ...
        }

        return response()->json($entities);
    }

    public static function deleteOne($id) {
        $entity = {{resourceClass}}::findOrFail($id);
        $entity->delete();

        return response()->json($entity);
    }