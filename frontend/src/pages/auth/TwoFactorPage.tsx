import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Loader2, Shield } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import toast from 'react-hot-toast';
import { getErrorMessage } from '@/lib/api';

export default function TwoFactorPage() {
  const [code, setCode] = useState(['', '', '', '', '', '']);
  const [isLoading, setIsLoading] = useState(false);
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
  const { loginWith2FA, requiresTwoFactor } = useAuth();
  const navigate = useNavigate();

  // Redirect if not in 2FA flow
  useEffect(() => {
    if (!requiresTwoFactor) {
      navigate('/login');
    }
  }, [requiresTwoFactor, navigate]);

  const handleChange = (index: number, value: string) => {
    // Only allow digits
    if (!/^\d*$/.test(value)) return;

    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);

    // Move to next input
    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }

    // Auto-submit when all digits are entered
    if (newCode.every((digit) => digit) && newCode.join('').length === 6) {
      handleSubmit(newCode.join(''));
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    // Move to previous input on backspace
    if (e.key === 'Backspace' && !code[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').slice(0, 6);
    if (!/^\d+$/.test(pastedData)) return;

    const newCode = [...code];
    pastedData.split('').forEach((digit, index) => {
      if (index < 6) newCode[index] = digit;
    });
    setCode(newCode);

    // Focus last filled input or first empty
    const lastIndex = Math.min(pastedData.length - 1, 5);
    inputRefs.current[lastIndex]?.focus();

    // Auto-submit if complete
    if (newCode.every((digit) => digit)) {
      handleSubmit(newCode.join(''));
    }
  };

  const handleSubmit = async (fullCode: string) => {
    if (fullCode.length !== 6) {
      toast.error('Please enter all 6 digits');
      return;
    }

    setIsLoading(true);
    try {
      await loginWith2FA(fullCode);
    } catch (error) {
      toast.error(getErrorMessage(error));
      setCode(['', '', '', '', '', '']);
      inputRefs.current[0]?.focus();
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <div className="mx-auto w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mb-4">
            <Shield className="h-8 w-8 text-primary-600" />
          </div>
          <h1 className="text-3xl font-bold text-gray-900">
            Two-Factor Authentication
          </h1>
          <p className="mt-2 text-gray-600">
            Enter the 6-digit code from your authenticator app
          </p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border p-8">
          <div className="flex justify-center gap-3 mb-8">
            {code.map((digit, index) => (
              <input
                key={index}
                ref={(el) => (inputRefs.current[index] = el)}
                type="text"
                inputMode="numeric"
                maxLength={1}
                value={digit}
                onChange={(e) => handleChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                onPaste={handlePaste}
                className="w-12 h-14 text-center text-xl font-bold border-2 rounded-lg focus:border-primary-500 focus:ring-primary-500 outline-none transition-colors"
                disabled={isLoading}
              />
            ))}
          </div>

          <button
            onClick={() => handleSubmit(code.join(''))}
            disabled={isLoading || code.some((d) => !d)}
            className="btn-primary w-full py-3"
          >
            {isLoading ? (
              <Loader2 className="h-5 w-5 animate-spin" />
            ) : (
              'Verify'
            )}
          </button>

          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              Didn't receive a code?{' '}
              <button className="text-primary-600 hover:underline">
                Use backup code
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
