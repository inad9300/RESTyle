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
        } elseif ($e instanceof ModelNotFoundException) {
            $e = new NotFoundHttpException($e->getMessage(), $e);
            $statusCode = HttpStatus::NotFound;
        } elseif ($e instanceof AuthorizationException) {
            $e = new HttpException(HttpStatus::Forbidden, $e->getMessage());
        } elseif ($e instanceof ValidationException && $e->getResponse()) {
            $errors = json_decode($e->getResponse()->content());
            $statusCode = HttpStatus::UnprocessableEntity;
        }

        if ($request->wantsJson()) {
            $response = [];
            /* $response = [
                'type' => '',
                'title' => 'Something went wrong', // IDEA: title should be based on HTTP status code's reason / thrown exception type
                'detail' => $e->getMessage() ?: 'No more information is known',
                'instance' => ''
            ]; */

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
        }

        return parent::render($request, $e);
    }
}