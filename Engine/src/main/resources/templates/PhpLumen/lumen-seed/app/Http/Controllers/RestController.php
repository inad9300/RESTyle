<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Laravel\Lumen\Routing\Controller as BaseController;

class RestController extends BaseController {

    const WILDCARD = '*';
    const FIELDS_SEP = ';';
    const VALUES_SEP = ',';

    const DEF_LIMIT = 50;

    /**
     * Generates an error message to be sent to the user.
     */
    protected static function error($message, $code = 422) {
        return response()->json($message, $code);
    }

    /**
     * Restricts the selected columns based on the given request.
     */
    protected static function addFieldsToQuery($query, Request $req) {
        if ($req->has('fields')) {
            $fields = explode(self::VALUES_SEP, $req->input('fields'));
            $query = $query->select($fields);
        }
        return $query;
    }

    /**
     * Adds sorting to the database query based on the given request.
     */
    protected static function addOrderByToQuery($query, Request $req, callable $isPropSortable) {
        if ($req->has('sort')) {
            $toSort = explode(self::VALUES_SEP, $req->input('sort'));

            foreach ($toSort as $prop) {
                $order = 'asc';

                if ($prop[0] === '-') {
                    $prop = ltrim($prop, '-');
                    $order = 'desc';
                } else if ($prop[0] === '+') {
                    $prop = ltrim($prop, '+');
                }
                
                if (call_user_func($isPropSortable, $prop)) {
                    $query = $query->orderBy($prop, $order);
                }
            }
        }
        return $query;
    }

    /**
     * Retrieves the limit present in the given request, or a default.
     */
    protected static function getLimit(Request $req) {
        return $req->has('limit') ? $req->input('limit') : self::DEF_LIMIT;
    }

    /**
     * Restricts the number of elements selected in the database based on the given request.
     */
    protected static function addLimitToQuery($query, Request $req) {
        return $query->take(self::getLimit($req));
    }

    /**
     * Adds some offset to the database query to support pagination.
     */
    protected static function addPageToQuery($query, Request $req) {
        if ($req->has('page')) {
            $page = $req->input('page');
            $query = $query->skip(($page - 1) * self::getLimit($req));
        }
        return $query;
    }

    /**
     * Extracts the different parts of the fields present in the filter query parameter.
     *
     * Intended use: list($fieldName, $operator, $values) = self::parseField($field);
     */
    protected static function parseField($field) {
        if (!preg_match('/([a-zA-Z_]{1,})\.(eq|neq|in|nin|lt|lte|gt|gte|bt|nbt)\((.{1,})\)/', $field, $matches)) {
            // NOTE: operator separator:  ^
            // ...
        }

        return [
            $matches[1],
            $matches[2],
            explode(self::VALUES_SEP, $matches[3])
        ];
    }

    /**
     * Adds conditions to the database query based on the given query.
     */
    protected static function addFilterToQuery($query, Request $req, callable $isPropFilterable) {
        if (!$req->has('filter')) {
            return $query;
        }

        $fields = explode(self::FIELDS_SEP, $req->input('filter'));

        foreach ($fields as $field) {
            list($name, $operator, $values) = self::parseField($field);

            if (!call_user_func($isPropFilterable, $name)) {
                continue;
            }

            $numOfValues = count($values);

            switch ($operator) {
            case 'eq':
                if ($numOfValues < 1) {
                    // ...
                }
                $query = $query->where(function ($q) use ($name, $values) {
                    foreach ($values as $value) {
                        if ($value === self::WILDCARD) {
                            continue;
                        }

                        if (strpos($value, self::WILDCARD) !== false) {
                            $q = $q->orWhere($name, 'like', str_replace(self::WILDCARD, '%', $value));
                        } else {
                            $q = $q->orWhere($name, '=', $value);
                        }
                    }
                });
                break;
            case 'neq':
                if ($numOfValues < 1) {
                    // ...
                }
                foreach ($values as $value) {
                    if ($value === self::WILDCARD) {
                        continue;
                    }

                    if (strpos($value, self::WILDCARD) !== false) {
                        $query = $query->where($name, 'not like', str_replace(self::WILDCARD, '%', $value));
                    } else {
                        $query = $query->where($name, '!=', $value);
                    }
                }
                break;
            case 'in':
                if ($numOfValues < 1) {
                    // ...
                }
                $query = $query->whereIn($name, $values);
                break;
            case 'nin':
                if ($numOfValues < 1) {
                    // ...
                }
                $query = $query->whereNotIn($name, $values);
                break;
            case 'lt':
                if ($numOfValues !== 1) {
                    // ...
                }
                $query = $query->where($name, '<', $values[0]);
                break;
            case 'lte':
                if ($numOfValues !== 1) {
                    // ...
                }
                $query = $query->where($name, '<=', $values[0]);
                break;
            case 'gt':
                if ($numOfValues !== 1) {
                    // ...
                }
                $query = $query->where($name, '>', $values[0]);
                break;
            case 'gte':
                if ($numOfValues !== 1) {
                    // ...
                }
                $query = $query->where($name, '>=', $values[0]);
                break;
            case 'bt':
                if ($numOfValues !== 2) {
                    // ...
                }
                $query = $query->whereBetween($name, $values);
                break;
            case 'nbt':
                if ($numOfValues !== 2) {
                    // ...
                }
                $query = $query->whereNotBetween($name, $values);
                break;
            default:
                // ...
                break;
            }
        }
        return $query;
    }
}
