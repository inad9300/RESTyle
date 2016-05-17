<?php

namespace App\Http\Controllers;

use DB;
use Gate;
use App\Utils\Arrays;
use App\Utils\HttpStatus;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

use App\Models{{resourceClassBack}};
{{#each relations}}use App\Models{{subresourceClassBack}};
{{/each}}
class {{resourceClass}}Controller extends RestController {

    // Resource end-point callbacks
    // ----------------------------

{{> Controller-resource}}{{! Indentation already in partial file }}
{{> Controller-files}}

    // Relationship end-points callbacks
    // ---------------------------------
    {{#each relations}}
    {{#unless isBelongTo}}{{#unless isOneToOne}}public function get{{subresourceClassPlural}}(Request $req, $entityId) {
        if (Gate::denies('read', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to read the resource'
            ]);
        }

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

        $related = $query->get();

        foreach ($related as &$ent) {
            self::addHalLink($ent, 'self', $req->path() . '/' . $ent->id);
        }
        {{#if isManyToMany}}
        foreach ($related as &$ent) {
            $ent->_embedded = (object) [
                'pivot' => $ent->pivot
            ];
            unset($ent->pivot);
        }{{/if}}

        $result = new \stdClass;
        $result->count = count($related);
        $result->total = $entity->{{subFn}}()->count();

        self::addQueryMade($result, $req);
        self::addHalPageLinks($result, $req, $result->total);
        self::addHalEmbedded($result, '{{subresourceNamePlural}}', $related);

        return response()->json($result);
    }{{/unless}}{{/unless}}
    {{#if isBelongTo}}
{{> Controller-getSingleResourceFunction}}{{/if}}
    {{#if isOneToOne}}
{{> Controller-getSingleResourceFunction}}{{/if}}

    public function create{{subresourceClassPlural}}(Request $req, $entityId) {
        if (Gate::denies('create', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to create the resource'
            ]);
        }

        $input = $req->all();

        if (Arrays::isAssoc($input)) {
            $this->validate($req, {{subresourceClass}}::getValidationRules());
        } else if (is_array($input)) {
            {{#if isOneToOne}}if (count($input) > 1) {
                return self::abort(HttpStatus::BadRequest, [
                    'title' => 'Too many resources provided',
                    'detail' => 'Only one resource representation is accepted. ' . count($input) . ' provided'
                ]);
            }
            $validator = Validator::make($input[0], {{subresourceClass}}::getValidationRules());
            if ($validator->fails()) {
                return self::abort(HttpStatus::UnprocessableEntity, ['invalid-fields' => $validator->messages()]);
            }{{else}}foreach ($input as $relatedEntity) {
                $validator = Validator::make($relatedEntity, {{subresourceClass}}::getValidationRules());
                if ($validator->fails()) {
                    return self::abort(HttpStatus::UnprocessableEntity, ['invalid-fields' => $validator->messages()]);
                }
            }{{/if}}
        } else {
            return self::abort(HttpStatus::BadRequest, [
                'title' => 'Wrong data format',
                'detail' => 'Only JSON objects and JSON arrays are valid'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($entityId);

        $relatedEntities = $entity->{{subFn}}()->create($input);

        return response()->json($relatedEntities);
    }
    {{#if isManyToMany}}
    public function getPivotWith{{subresourceClass}}($entityId, $relatedEntityId) {
        if (Gate::denies('read', new {{resourceClass}}()) || Gate::denies('read', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to get the resource'
            ]);
        }

        $pivot = (object) null;
        $entity = {{resourceClass}}::findOrFail($entityId);
        foreach ($entity->{{subFn}} as $relatedEntity) {
            if ($relatedEntity->id === (int) $relatedEntityId) {
                $pivot = $relatedEntity->pivot;
                break;
            }
        }

        return response()->json($pivot);
    }

    public function unlink{{subresourceClass}}($entityId, $relatedEntityId) {
        if (Gate::denies('delete', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to unlink the resource'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($entityId);
        foreach ($entity->{{subFn}} as $relatedEntity) {
            if ($relatedEntity->{{subresourceId}} === (int) $relatedEntityId) {
                $pivot = $relatedEntity->pivot;
                break;
            }
        }
        $entity->{{subFn}}()->detach($relatedEntityId);

        return response()->json($pivot);
    }

    public function unlink{{subresourceClassPlural}}(Request $req, $entityId) {
        if (Gate::denies('delete', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to unlink the resource'
            ]);
        }

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
            return self::abort(HttpStatus::BadRequest, [
                'title' => 'Wrong data format',
                'detail' => 'Either an empty body or a numeric array must be provided'
            ]);
        }

        return response()->json($pivots);
    }{{else}}
    public function delete{{subresourceClass}}($entityId, $relatedEntityId) {
        if (Gate::denies('delete', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to delete the resource'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($entityId);
        $relatedEntity = $entity->{{subFn}}()->findOrFail($relatedEntityId);
        {{subresourceClass}}::destroy($relatedEntity->{{subresourceId}});

        return response()->json($relatedEntity);
    }

    public function delete{{subresourceClassPlural}}(Request $req, $entityId) {
        if (Gate::denies('delete', new {{subresourceClass}}())) {
            return self::abort(HttpStatus::Forbidden, [
                'title' => 'Not allowed to delete the resource'
            ]);
        }

        $entity = {{resourceClass}}::findOrFail($entityId);
        {{#if isOneToOne}}
        $related = $entity->{{subFn}};
        $entity->{{subFn}}()->delete();{{else}}
        if (!$req->getContent()) {
            $related = $entity->{{subFn}};
            $entity->{{subFn}}()->delete();
        } else if (is_array($req->all())) {
            $related = $entity->{{subFn}}->findOrFail($req->all());
            {{subresourceClass}}::destroy($req->all());
        } else {
            return self::abort(HttpStatus::BadRequest, [
                'title' => 'Wrong data format',
                'detail' => 'Either an empty body or a numeric array must be provided'
            ]);
        }{{/if}}

        return response()->json($related);
    }{{/if}}
    {{/each}}
}
