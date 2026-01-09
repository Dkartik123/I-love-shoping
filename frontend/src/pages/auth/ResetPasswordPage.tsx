import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Loader2, Check, X } from 'lucide-react';
import { authApi } from '@/services/authApi';
import toast from 'react-hot-toast';
import { getErrorMessage } from '@/lib/api';

const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/;

const resetPasswordSchema = z
  .object({
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(
        passwordRegex,
        'Password must contain uppercase, lowercase, number, and special character'
      ),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;

export default function ResetPasswordPage() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const token = searchParams.get('token');

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
  });

  const password = watch('password', '');

  const passwordChecks = {
    length: password.length >= 8,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number: /\d/.test(password),
    special: /[@$!%*?&]/.test(password),
  };

  const onSubmit = async (data: ResetPasswordFormData) => {
    if (!token) {
      toast.error('Invalid reset token');
      return;
    }

    setIsLoading(true);
    try {
      await authApi.resetPassword(token, data.password, data.confirmPassword);
      toast.success('Password reset successfully');
      navigate('/login');
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  };

  if (!token) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            Invalid Reset Link
          </h1>
          <p className="text-gray-600 mb-8">
            The password reset link is invalid or has expired. Please request a
            new one.
          </p>
          <button
            onClick={() => navigate('/forgot-password')}
            className="btn-primary px-8 py-3"
          >
            Request New Link
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Set new password</h1>
          <p className="mt-2 text-gray-600">
            Your new password must be different from previous passwords
          </p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border p-8">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div>
              <label htmlFor="password" className="label">
                New password
              </label>
              <div className="relative">
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  autoComplete="new-password"
                  className={errors.password ? 'input-error pr-10' : 'input pr-10'}
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showPassword ? (
                    <EyeOff className="h-5 w-5" />
                  ) : (
                    <Eye className="h-5 w-5" />
                  )}
                </button>
              </div>
              
              {/* Password strength indicators */}
              <div className="mt-2 space-y-1 text-sm">
                {Object.entries({
                  'At least 8 characters': passwordChecks.length,
                  'One uppercase letter': passwordChecks.uppercase,
                  'One lowercase letter': passwordChecks.lowercase,
                  'One number': passwordChecks.number,
                  'One special character (@$!%*?&)': passwordChecks.special,
                }).map(([label, valid]) => (
                  <div
                    key={label}
                    className={`flex items-center space-x-2 ${
                      valid ? 'text-green-600' : 'text-gray-400'
                    }`}
                  >
                    {valid ? (
                      <Check className="h-4 w-4" />
                    ) : (
                      <X className="h-4 w-4" />
                    )}
                    <span>{label}</span>
                  </div>
                ))}
              </div>
            </div>

            <div>
              <label htmlFor="confirmPassword" className="label">
                Confirm new password
              </label>
              <div className="relative">
                <input
                  {...register('confirmPassword')}
                  type={showConfirmPassword ? 'text' : 'password'}
                  id="confirmPassword"
                  autoComplete="new-password"
                  className={
                    errors.confirmPassword ? 'input-error pr-10' : 'input pr-10'
                  }
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                >
                  {showConfirmPassword ? (
                    <EyeOff className="h-5 w-5" />
                  ) : (
                    <Eye className="h-5 w-5" />
                  )}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="error-message">{errors.confirmPassword.message}</p>
              )}
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="btn-primary w-full py-3"
            >
              {isLoading ? (
                <Loader2 className="h-5 w-5 animate-spin" />
              ) : (
                'Reset password'
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
