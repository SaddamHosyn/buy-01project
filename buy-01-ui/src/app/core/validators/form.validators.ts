/**
 * Custom Form Validators
 * Provides custom validation functions for form controls with user-friendly error messages
 */

import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Price validator - validates positive numbers with optional decimal places
 * @param min Minimum price (default: 0.01)
 * @param max Maximum price (optional)
 * @param maxDecimals Maximum decimal places (default: 2)
 */
export function priceValidator(
  min: number = 0.01,
  max?: number,
  maxDecimals: number = 2
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    // Allow empty value (use Validators.required separately)
    if (value === null || value === undefined || value === '') {
      return null;
    }
    
    const numValue = Number(value);
    
    // Check if valid number
    if (isNaN(numValue)) {
      return { invalidPrice: { message: 'Price must be a valid number' } };
    }
    
    // Check minimum price
    if (numValue < min) {
      return {
        minPrice: {
          min: min,
          actual: numValue,
          message: `Price must be at least €${min.toFixed(2)}`
        }
      };
    }
    
    // Check maximum price
    if (max !== undefined && numValue > max) {
      return {
        maxPrice: {
          max: max,
          actual: numValue,
          message: `Price cannot exceed €${max.toFixed(2)}`
        }
      };
    }
    
    // Check decimal places
    const decimalPart = value.toString().split('.')[1];
    if (decimalPart && decimalPart.length > maxDecimals) {
      return {
        maxDecimals: {
          maxDecimals: maxDecimals,
          actual: decimalPart.length,
          message: `Price can have maximum ${maxDecimals} decimal places`
        }
      };
    }
    
    // Check for negative zero
    if (Object.is(numValue, -0)) {
      return { invalidPrice: { message: 'Price cannot be negative' } };
    }
    
    return null;
  };
}

/**
 * Positive number validator - validates positive integers or decimals
 * @param allowZero Whether to allow zero (default: false)
 */
export function positiveNumberValidator(allowZero: boolean = false): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (value === null || value === undefined || value === '') {
      return null;
    }
    
    const numValue = Number(value);
    
    if (isNaN(numValue)) {
      return { invalidNumber: { message: 'Must be a valid number' } };
    }
    
    if (allowZero && numValue < 0) {
      return {
        positiveNumber: {
          message: 'Must be a positive number or zero'
        }
      };
    }
    
    if (!allowZero && numValue <= 0) {
      return {
        positiveNumber: {
          message: 'Must be a positive number greater than zero'
        }
      };
    }
    
    return null;
  };
}

/**
 * Integer validator - validates whole numbers only
 */
export function integerValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (value === null || value === undefined || value === '') {
      return null;
    }
    
    const numValue = Number(value);
    
    if (isNaN(numValue)) {
      return { invalidNumber: { message: 'Must be a valid number' } };
    }
    
    if (!Number.isInteger(numValue)) {
      return {
        integer: {
          message: 'Must be a whole number (no decimals)'
        }
      };
    }
    
    return null;
  };
}

/**
 * Range validator - validates number within a specific range
 * @param min Minimum value (inclusive)
 * @param max Maximum value (inclusive)
 */
export function rangeValidator(min: number, max: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (value === null || value === undefined || value === '') {
      return null;
    }
    
    const numValue = Number(value);
    
    if (isNaN(numValue)) {
      return { invalidNumber: { message: 'Must be a valid number' } };
    }
    
    if (numValue < min || numValue > max) {
      return {
        range: {
          min: min,
          max: max,
          actual: numValue,
          message: `Must be between ${min} and ${max}`
        }
      };
    }
    
    return null;
  };
}

/**
 * URL validator - validates URL format
 * @param requireProtocol Whether to require http:// or https:// (default: true)
 */
export function urlValidator(requireProtocol: boolean = true): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    try {
      const url = new URL(value);
      
      if (requireProtocol && !['http:', 'https:'].includes(url.protocol)) {
        return {
          invalidUrl: {
            message: 'URL must start with http:// or https://'
          }
        };
      }
      
      return null;
    } catch {
      return {
        invalidUrl: {
          message: 'Must be a valid URL'
        }
      };
    }
  };
}

/**
 * Phone number validator - validates phone number format
 * Supports various formats: (123) 456-7890, 123-456-7890, 1234567890, +1 123 456 7890
 */
export function phoneValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    // Remove all non-digit characters for validation
    const digitsOnly = value.replace(/\D/g, '');
    
    // Check if it has 10-15 digits (allows for country codes)
    if (digitsOnly.length < 10 || digitsOnly.length > 15) {
      return {
        invalidPhone: {
          message: 'Phone number must contain 10-15 digits'
        }
      };
    }
    
    return null;
  };
}

/**
 * Match validator - validates that two controls have the same value
 * @param controlName Name of the control to match
 * @param matchingControlName Name of the control to match against
 */
export function matchValidator(controlName: string, matchingControlName: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const formGroup = control.parent;
    
    if (!formGroup) {
      return null;
    }
    
    const controlToMatch = formGroup.get(controlName);
    const matchingControl = formGroup.get(matchingControlName);
    
    if (!controlToMatch || !matchingControl) {
      return null;
    }
    
    if (controlToMatch.value !== matchingControl.value) {
      return {
        mismatch: {
          message: `${formatFieldName(controlName)} and ${formatFieldName(matchingControlName)} do not match`
        }
      };
    }
    
    return null;
  };
}

/**
 * No whitespace validator - validates that value contains no whitespace
 */
export function noWhitespaceValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    if (/\s/.test(value)) {
      return {
        whitespace: {
          message: 'Must not contain whitespace'
        }
      };
    }
    
    return null;
  };
}

/**
 * Alphanumeric validator - validates that value contains only letters and numbers
 * @param allowSpaces Whether to allow spaces (default: false)
 */
export function alphanumericValidator(allowSpaces: boolean = false): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    const pattern = allowSpaces ? /^[a-zA-Z0-9\s]+$/ : /^[a-zA-Z0-9]+$/;
    
    if (!pattern.test(value)) {
      return {
        alphanumeric: {
          message: allowSpaces 
            ? 'Must contain only letters, numbers, and spaces'
            : 'Must contain only letters and numbers'
        }
      };
    }
    
    return null;
  };
}

/**
 * Strong password validator - validates password strength
 * Requires: min 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
 */
export function strongPasswordValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    const errors: string[] = [];
    
    if (value.length < 8) {
      errors.push('at least 8 characters');
    }
    
    if (!/[A-Z]/.test(value)) {
      errors.push('one uppercase letter');
    }
    
    if (!/[a-z]/.test(value)) {
      errors.push('one lowercase letter');
    }
    
    if (!/[0-9]/.test(value)) {
      errors.push('one number');
    }
    
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)) {
      errors.push('one special character');
    }
    
    if (errors.length > 0) {
      return {
        weakPassword: {
          message: `Password must contain ${errors.join(', ')}`
        }
      };
    }
    
    return null;
  };
}

/**
 * Date range validator - validates that date is within a specific range
 * @param minDate Minimum date (inclusive)
 * @param maxDate Maximum date (inclusive)
 */
export function dateRangeValidator(minDate?: Date, maxDate?: Date): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    const date = new Date(value);
    
    if (isNaN(date.getTime())) {
      return {
        invalidDate: {
          message: 'Must be a valid date'
        }
      };
    }
    
    if (minDate && date < minDate) {
      return {
        minDate: {
          min: minDate,
          actual: date,
          message: `Date must be on or after ${formatDate(minDate)}`
        }
      };
    }
    
    if (maxDate && date > maxDate) {
      return {
        maxDate: {
          max: maxDate,
          actual: date,
          message: `Date must be on or before ${formatDate(maxDate)}`
        }
      };
    }
    
    return null;
  };
}

/**
 * Future date validator - validates that date is in the future
 * @param allowToday Whether to allow today's date (default: false)
 */
export function futureDateValidator(allowToday: boolean = false): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    
    if (!value) {
      return null;
    }
    
    const date = new Date(value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (isNaN(date.getTime())) {
      return {
        invalidDate: {
          message: 'Must be a valid date'
        }
      };
    }
    
    if (allowToday) {
      if (date < today) {
        return {
          futureDate: {
            message: 'Date must be today or in the future'
          }
        };
      }
    } else {
      if (date <= today) {
        return {
          futureDate: {
            message: 'Date must be in the future'
          }
        };
      }
    }
    
    return null;
  };
}

/**
 * Helper function to format field names
 */
function formatFieldName(name: string): string {
  return name
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (str) => str.toUpperCase())
    .trim();
}

/**
 * Helper function to format dates
 */
function formatDate(date: Date): string {
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}

/**
 * Get user-friendly error message from validation errors
 * @param errors Validation errors object
 * @param fieldName Name of the field (optional)
 */
export function getValidationMessage(errors: ValidationErrors | null, fieldName?: string): string {
  if (!errors) {
    return '';
  }
  
  const field = fieldName ? formatFieldName(fieldName) : 'This field';
  
  // Built-in Angular validators
  if (errors['required']) {
    return `${field} is required`;
  }
  
  if (errors['email']) {
    return 'Please enter a valid email address';
  }
  
  if (errors['minlength']) {
    const required = errors['minlength'].requiredLength;
    return `${field} must be at least ${required} characters`;
  }
  
  if (errors['maxlength']) {
    const required = errors['maxlength'].requiredLength;
    return `${field} must not exceed ${required} characters`;
  }
  
  if (errors['min']) {
    const min = errors['min'].min;
    return `${field} must be at least ${min}`;
  }
  
  if (errors['max']) {
    const max = errors['max'].max;
    return `${field} must not exceed ${max}`;
  }
  
  if (errors['pattern']) {
    return `${field} has an invalid format`;
  }
  
  // Custom validators (they include their own messages)
  if (errors['invalidPrice']?.message) return errors['invalidPrice'].message;
  if (errors['minPrice']?.message) return errors['minPrice'].message;
  if (errors['maxPrice']?.message) return errors['maxPrice'].message;
  if (errors['maxDecimals']?.message) return errors['maxDecimals'].message;
  if (errors['positiveNumber']?.message) return errors['positiveNumber'].message;
  if (errors['integer']?.message) return errors['integer'].message;
  if (errors['range']?.message) return errors['range'].message;
  if (errors['invalidUrl']?.message) return errors['invalidUrl'].message;
  if (errors['invalidPhone']?.message) return errors['invalidPhone'].message;
  if (errors['mismatch']?.message) return errors['mismatch'].message;
  if (errors['whitespace']?.message) return errors['whitespace'].message;
  if (errors['alphanumeric']?.message) return errors['alphanumeric'].message;
  if (errors['weakPassword']?.message) return errors['weakPassword'].message;
  if (errors['invalidDate']?.message) return errors['invalidDate'].message;
  if (errors['minDate']?.message) return errors['minDate'].message;
  if (errors['maxDate']?.message) return errors['maxDate'].message;
  if (errors['futureDate']?.message) return errors['futureDate'].message;
  if (errors['invalidNumber']?.message) return errors['invalidNumber'].message;
  
  // Fallback
  return `${field} is invalid`;
}
