import { useAuth } from '@/context/AuthContext';

export default function ProfilePage() {
  const { user } = useAuth();

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">My Profile</h1>

      <div className="bg-white rounded-xl shadow-sm border p-6">
        <div className="flex items-center space-x-6 mb-6">
          <div className="w-20 h-20 bg-primary-100 rounded-full flex items-center justify-center">
            <span className="text-2xl font-bold text-primary-600">
              {user?.firstName?.[0]}
              {user?.lastName?.[0]}
            </span>
          </div>
          <div>
            <h2 className="text-xl font-semibold">
              {user?.firstName} {user?.lastName}
            </h2>
            <p className="text-gray-500">{user?.email}</p>
          </div>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          <div>
            <label className="label">First Name</label>
            <input
              type="text"
              value={user?.firstName || ''}
              readOnly
              className="input bg-gray-50"
            />
          </div>
          <div>
            <label className="label">Last Name</label>
            <input
              type="text"
              value={user?.lastName || ''}
              readOnly
              className="input bg-gray-50"
            />
          </div>
          <div>
            <label className="label">Email</label>
            <input
              type="email"
              value={user?.email || ''}
              readOnly
              className="input bg-gray-50"
            />
          </div>
          <div>
            <label className="label">Phone</label>
            <input
              type="tel"
              value={user?.phone || 'Not provided'}
              readOnly
              className="input bg-gray-50"
            />
          </div>
        </div>

        <div className="mt-8 pt-6 border-t">
          <h3 className="font-semibold mb-4">Security</h3>
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium">Two-Factor Authentication</p>
              <p className="text-sm text-gray-500">
                {user?.twoFactorEnabled
                  ? 'Enabled - your account is more secure'
                  : 'Not enabled - add an extra layer of security'}
              </p>
            </div>
            <button className="btn-outline">
              {user?.twoFactorEnabled ? 'Disable' : 'Enable'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
