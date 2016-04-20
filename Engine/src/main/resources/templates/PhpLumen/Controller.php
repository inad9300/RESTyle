<?php

namespace App\Http\Controllers;

use DB;
use Gate;
use App\Utils\Arrays;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

use App\Models{{resourceClassBack}};
{{#each relations}}use App\Models{{subresourceClassBack}};
{{/each}}
class {{resourceClass}}Controller extends RestController {

    // Resource end-point callbacks
    // ----------------------------

{{> Controller-resource}}{{! Indentation already in partial file }}


    // Relationship end-points callbacks
    // ---------------------------------
    {{#each relations}}
    public static function get{{subresourceClassPlural}}(Request $req, $entityId) {
        $entity = {{resourceClass}}::findOrFail($entityId);

        $query = $entity->{{subFn}}();

        $query = self::addFieldsToQuery($query, $req);

        $query = self::addOrderByToQuery($query, $req, function ($prop) {
            return {{subresourceClass}}::isSortable($prop);
        });

        $query = self::addLimitToQuery($query, $req);

        $query = self::addPageToQuery($query, $req);

        $query = self::addFilterToQuery($query, $req, function ($prop) {
            return {{subresourceClass}}::isFilterable($prop);
        });

        $entities = $query->get();

        return response()->json($entities);
    }

    public static function create{{subresourceClassPlural}}(Request $req, $entityId) {
        $input = $req->all();

        if (Arrays::isAssoc($input)) {
            $this->validate($req, {{subresourceClass}}::getValidationRules());
        } else if (is_array($input)) {
            {{#if isOneToX}}if (count($input) > 1) {
                // ...
            }
            $validator = Validator::make($input[0], {{subresourceClass}}::getValidationRules());
            if ($validator->fails()) {
                return self::error($validator->messages());
            }{{else}}foreach ($input as $relatedEntity) {
                $validator = Validator::make($relatedEntity, {{subresourceClass}}::getValidationRules());
                if ($validator->fails()) {
                    return self::error($validator->messages());
                }
            }{{/if}}
        } else {
            // ...
        }

        $entity = {{resourceClass}}::findOrFail($entityId);

        $relatedEntities = $entity->{{subFn}}()->create($input);

        return response()->json($relatedEntities);
    }

    {{#if isManyToMany}}public static function unlink{{subresourceClass}}($entityId, $relatedEntityId) {
        $entity = {{resourceClass}}::findOrFail($entityId);
        foreach ($entity->{{subFn}} as $relatedEntity) {
            if ($relatedEntity->{{subresourceId}} === $relatedEntityId) {
                $pivot = $relatedEntity->pivot;
                break;
            }
        }
        $entity->{{subFn}}()->detach($relatedEntityId);

        return response()->json($pivot);
    }

    public static function unlink{{subresourceClassPlural}}(Request $req, $entityId) {
        $entity = {{resourceClass}}::findOrFail($entityId);
        $pivots = [];

        if (!$req->getContent()) {
            foreach ($entity->{{subFn}} as $relatedEntity) {
                $pivots[] = $relatedEntity->pivot;
            }
            $entity->{{subFn}}()->detach();
        } else if (is_array($req->all())) {
            foreach ($entity->{{subFn}} as $relatedEntity) {
                if (in_array($relatedEntity->{{subresourceId}}, $req->all())) {
                    $pivots[] = $relatedEntity->pivot;
                }
            }
            $entity->{{subFn}}()->detach($req-all());
        } else {
            // ...
        }

        return response()->json($pivots);
    }{{else}}public static function delete{{subresourceClass}}($entityId, $relatedEntityId) {
        $entity = {{resourceClass}}::findOrFail($entityId);
        $relatedEntity = $entity->{{subFn}}()->findOrFail($relatedEntityId);
        {{subresourceClass}}::destroy($relatedEntity->{{subresourceId}});

        return response()->json($relatedEntity);
    }

    public static function delete{{subresourceClassPlural}}(Request $req, $entityId) {
        $entity = {{resourceClass}}::findOrFail($entityId);

        {{#if isOneToX}}$related = $entity->{{subFn}};
        $entity->{{subFn}}()->delete();{{else}}if (!$req->getContent()) {
            $related = $entity->{{subFn}};
            $entity->{{subFn}}()->delete();
        } else if (is_array($req->all())) {
            $related = $entity->{{subFn}}->findOrFail($req->all());
            {{subresourceClass}}::destroy($req->all());
        } else {
            // ...
        }{{/if}}

        return response()->json($related);
    }{{/if}}
    {{/each}}
}
