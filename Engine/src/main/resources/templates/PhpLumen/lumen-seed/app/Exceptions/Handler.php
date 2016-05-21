<?php

namespace App\Exceptions;

use Exception;
use App\Utils\HttpStatus;
use Illuminate\Validation\ValidationException;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Symfony\Component\HttpKernel\Exception\HttpException;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Laravel\Lumen\Exceptions\Handler as ExceptionHandler;

class Handler extends ExceptionHandler {
    /**
     * A list of the exception types that should not be reported.
     *
     * @var array
     */
    protected $dontReport = [
        AuthorizationException::class,
        HttpException::class,
        ModelNotFoundException::class,
        ValidationException::class,
    ];

    /**
     * Report or log an exception.
     *
     * This is a great spot to send exceptions to Sentry, Bugsnag, etc.
     *
     * @param  \Exception  $e
     * @return void
     */
    public function report(Exception $e) {
        parent::report($e);
    }

    /**
     * Render an exception into an HTTP response. Should conform to RFC "Problem Details for HTTP APIs":
     * https://tools.ietf.org/html/rfc7807
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \Exception  $e
     * @return \Illuminate\Http\Response
     */
    public function render($request, Exception $e) {
        if ($e instanceof HttpResponseException) {
            // return $e->getResponse();
            $title = 'An exception happened when preparing the HTTP response';
        } elseif ($e instanceof ModelNotFoundException) {
            $e = new NotFoundHttpException($e->getMessage(), $e);
            $statusCode = HttpStatus::NotFound;
            $title = 'The entity does not exist';
        } elseif ($e instanceof AuthorizationException) {
            $e = new HttpException(HttpStatus::Forbidden, $e->getMessage());
            $title = 'You are not authorized to access this information';
        } elseif ($e instanceof ValidationException && $e->getResponse()) {
            $errors = json_decode($e->getResponse()->content());
            $statusCode = HttpStatus::UnprocessableEntity;
            $title = 'Data validation error';
        }

        $response = [];
        /* $response = [
            'type' => '',
            'title' => 'Something went wrong',
            'detail' => $e->getMessage() ?: 'No more information is known',
            'instance' => ''
        ]; */

        if (isset($title)) {
            $response['title'] = $title;
        }
        if ($e->getMessage()) {
            $response['detail'] = $e->getMessage();
        }
        if (isset($errors)) {
            $response['invalid-fields'] = $errors;
        }

        if (env('APP_DEBUG', false)) {
            $response['debug'] = [
                'exception' => get_class($e),
                'trace' => $e->getTrace(),
                'response' => method_exists($e, 'getResponse') ? $e->getResponse() : ''
            ];
        }

        if (!isset($statusCode)) {
            if (method_exists($e, 'getStatusCode')) {
                $statusCode = $e->getStatusCode();
            } else {
                $statusCode = HttpStatus::InternalServerError;
            }
        }

        return response()->json($response, $statusCode);

        // return parent::render($request, $e);
    }
}