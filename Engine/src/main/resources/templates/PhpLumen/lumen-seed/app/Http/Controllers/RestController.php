<?php

namespace App\Http\Controllers;

use App\Utils\Arrays;
use App\Utils\HttpStatus;
use Illuminate\Http\Request;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Exception\HttpResponseException;
use Laravel\Lumen\Routing\Controller as BaseController;

class RestController extends BaseController {

    const WILDCARD = '*';
    const FIELDS_SEP = ';';
    const VALUES_SEP = ',';

    const DEF_PAGE = 1;
    const DEF_LIMIT = 50;

    /**
     * Produces an HTTP response which should conform to RFC "Problem Details for HTTP APIs":
     * https://tools.ietf.org/html/rfc7807
     *
     * It is presented as an alternative to Laravel's abort(), and to throwing/handling exceptions.
     * In summary, $data should contain the keys 'type', 'title', 'detail' and 'instance', plus any
     * extra problem-specific information.
     */
    protected static function abort($code = HttpStatus::BadRequest, $data = []) {
        // $data['type'] = $data['type'] ?: '';
        // $data['title'] = $data['title'] ?: 'Something went wrong';
        // $data['detail'] = $data['detail'] ?: 'No more information is known';
        // $data['instance'] = $data['instance'] ?: '';

        return response()->json($data, $code);
    }

    /**
     * Same idea as abort(), but for internal use in the controller.
     */
    private static function error($code = HttpStatus::BadRequest, $data = []) {
        throw new HttpResponseException(new JsonResponse($data, $code));
    }

    /**
     * Add a link to an object as in HAL:
     * http://stateless.co/hal_specification.html,
     * http://phlyrestfully.readthedocs.io/en/latest/halprimer.html
     */
    protected static function addHalLink(&$obj, $key, $val) {
        if (!property_exists($obj, '_links') || empty($obj->_links)) {
            $obj->_links = new \stdClass;
        }

        $obj->_links->{$key} = (object) [
            'href' => '/' . trim($val, '/')
        ];
    }

    /**
     * Add HAL links related with pagination.
     */
    protected static function addHalPageLinks(&$obj, Request $req, $totalItems) {
        $path = '/' . trim($req->path(), '/');
        $queryStrObj = $req->query();

        self::addHalLink($obj, 'self', $path . (count($queryStrObj) > 0 ? '?' : '') . http_build_query($queryStrObj));

        $queryStrObj['page'] = 1;
        self::addHalLink($obj, 'first', $path . (count($queryStrObj) > 0 ? '?' : '') . http_build_query($queryStrObj));

        $lastPage = ceil($totalItems / (!empty($req->query('limit')) ? $req->query('limit') : self::DEF_LIMIT));
        $lastPage = $lastPage ?: 1;
        $queryStrObj['page'] = $lastPage;
        self::addHalLink($obj, 'last', $path . (count($queryStrObj) > 0 ? '?' : '') . http_build_query($queryStrObj));

        $queryStrObj['page'] = $req->query('page') <= 1 ? 1 : $req->query('page') - 1;
        self::addHalLink($obj, 'previous', $path . (count($queryStrObj) > 0 ? '?' : '') . http_build_query($queryStrObj));

        $queryStrObj['page'] = $req->query('page') >= $lastPage ? $lastPage : $req->query('page') + 1;
        self::addHalLink($obj, 'next', $path . (count($queryStrObj) > 0 ? '?' : '') . http_build_query($queryStrObj));
    }

    /**
     * Add a embedded collection to an object as in HAL:
     * http://stateless.co/hal_specification.html,
     * http://phlyrestfully.readthedocs.io/en/latest/halprimer.html
     */
    protected static function addHalEmbedded(&$obj, $name, $data) {
        if (!property_exists($obj, '_embedded') || empty($obj->_embedded)) {
            $obj->_embedded = new \stdClass;
        }

        $obj->_embedded->{$name} = $data;
    }

    /**
     * Selects those rules referring to the properties present in the given data. Suitable for
     * partial updates.
     */
    protected static function filterValidationRules(array $rules, Request $req) {
        return array_intersect_key(
            $rules,
            array_flip(array_keys($req->all()))
        );
    }

    /**
     * Add the query string parameters given by the user, merged with the default ones used,
     * so that the user knows exactly how the query was performed.
     */
    protected static function addQueryMade(&$obj, Request $req) {
        $queryStrObj = $req->query();

        // Add defaults
        if (empty($queryStrObj['limit'])) {
            $queryStrObj['limit'] = self::DEF_LIMIT;
        }
        if (empty($queryStrObj['page'])) {
            $queryStrObj['page'] = self::DEF_PAGE;
        }

        $queryStrObj['page'] = (int) $queryStrObj['page'];
        $queryStrObj['limit'] = (int) $queryStrObj['limit'];

        $obj->query = (object) $queryStrObj;
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
            self::error(HttpStatus::BadRequest, [
                'title' => 'Wrong query format; please, double-check it',
                'detail' => 'The provided query does not conform to the expected format. Valid queries must follow: ' .
                            '{attribute}.{operator}({values})'
            ]);
        }

        return [
            $matches[1],
            $matches[2],
            explode(self::VALUES_SEP, $matches[3])
        ];
    }

    /**
     * Adds conditions to the database query based on the given query (from the query string of the
     * HTTP request).
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
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Some value is needed for equality comparison'
                    ]);
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
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Some value is needed for non-equality comparison'
                    ]);
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
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Some value is needed for inclusion comparisons, none provided'
                    ]);
                }
                $query = $query->whereIn($name, $values);
                break;
            case 'nin':
                if ($numOfValues < 1) {
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Some value is needed for non-inclusion comparisons, none provided'
                    ]);
                }
                $query = $query->whereNotIn($name, $values);
                break;
            case 'lt':
                if ($numOfValues !== 1) {
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Exactly one value is needed for is-less-than comparisons'
                    ]);
                }
                $query = $query->where($name, '<', $values[0]);
                break;
            case 'lte':
                if ($numOfValues !== 1) {
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Exactly one value is needed for is-less-or-equals-than comparisons'
                    ]);
                }
                $query = $query->where($name, '<=', $values[0]);
                break;
            case 'gt':
                if ($numOfValues !== 1) {
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Exactly one value is needed for is-greater-than comparisons'
                    ]);
                }
                $query = $query->where($name, '>', $values[0]);
                break;
            case 'gte':
                if ($numOfValues !== 1) {
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Exactly one value is needed for is-greater-or-equal-than comparisons'
                    ]);
                }
                $query = $query->where($name, '>=', $values[0]);
                break;
            case 'bt':
                if ($numOfValues !== 2) {
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Exactly two values are needed for is-between comparisons'
                    ]);
                }
                $query = $query->whereBetween($name, $values);
                break;
            case 'nbt':
                if ($numOfValues !== 2) {
                    self::error(HttpStatus::BadRequest, [
                        'title' => 'Exactly two values are needed for is-not-between comparisons'
                    ]);
                }
                $query = $query->whereNotBetween($name, $values);
                break;
            default:
                self::error(HttpStatus::BadRequest, [
                    'title' => "Unknown operator used: '$operator'"
                ]);
                break;
            }
        }
        return $query;
    }
}
