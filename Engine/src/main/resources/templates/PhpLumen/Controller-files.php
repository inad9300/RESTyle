{{#if resourceFiles}}

    // File-dealing end-point callbacks
    // --------------------------------
    {{#each resourceFiles}}
    public function get{{camelName}}File(Request $req, $id) {
        if (Gate::denies('read', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to read the file'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($id);

        $fileContent = $entity->getOriginal('{{name}}');

        if ($fileContent === null) {
            return self::abort(HttpStatus::NotFound, [
                'title' => 'File not found',
                'detail' => 'The resource does not appear to have any file stored as {{name}}'
            ]);
        }

        $finfo = new \finfo(FILEINFO_MIME_TYPE);
        $mimeType = $finfo->buffer($fileContent);

        return response($fileContent)
            // ->header('Content-Disposition', 'inline; filename="..."')
            // ->header('Content-Transfer-Encoding', 'binary')
            ->header('Content-Type', $mimeType);
    }

    public function upload{{camelName}}File(Request $req, $id) {
        if (Gate::denies('create', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to create the file'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($id);

        $entity->{{name}} = file_get_contents('php://input');

        $entity->save();

        $finfo = new \finfo(FILEINFO_MIME);
        $mimeType = $finfo->buffer(file_get_contents('php://input'));

        return response()->json($entity);
    }

    public function delete{{camelName}}File(Request $req, $id) {
        if (Gate::denies('delete', new {{resourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to delete the file'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($id);

        $entity->{{name}} = null;
        $entity->save();

        return response()->json($entity);
    }{{/each}}
{{/if}}