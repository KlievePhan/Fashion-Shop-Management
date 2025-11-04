package org.fsm.exception;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.BindException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

/**
 * Simple exception handler for Thymeleaf / MVC controllers.
 * Apply only to controllers annotated with @Controller.
 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ModelAndView handleAppException(AppException ex, WebRequest request) {
        ModelAndView mav = new ModelAndView("error/custom-error");
        mav.addObject("status", ex.getStatus().value());
        mav.addObject("error", ex.getStatus().getReasonPhrase());
        mav.addObject("message", ex.getMessage());
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }

    @ExceptionHandler(BindException.class)
    public ModelAndView handleBindException(BindException ex, WebRequest request) {
        ModelAndView mav = new ModelAndView(getViewNameFromRequest(request));
        mav.addObject("errors", ex.getFieldErrors());
        mav.addObject("message", "Please correct the errors below.");
        return mav;
    }

    // Simple helper to guess which form view to return
    private String getViewNameFromRequest(WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        if (path.contains("/add") || path.contains("/edit")) {
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash > 0) {
                return path.substring(0, lastSlash) + "/form";
            }
        }
        return "error/form-error";
    }
}
