import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Heart, ShoppingCart, User, Menu, X, Search } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';
import SearchBar from '@/components/search/SearchBar';

export default function Header() {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      {/* Top bar */}
      <div className="bg-primary-600 text-white text-sm py-2">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex justify-between items-center">
          <p>Free shipping on orders over $50!</p>
          <div className="hidden md:flex items-center space-x-4">
            <a href="tel:+1234567890" className="hover:underline">
              +1 (234) 567-890
            </a>
            <span>|</span>
            <a href="mailto:support@iloveshopping.com" className="hover:underline">
              support@iloveshopping.com
            </a>
          </div>
        </div>
      </div>

      {/* Main header */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2">
            <Heart className="h-8 w-8 text-primary-600 fill-primary-600" />
            <span className="text-xl font-bold text-gray-900">
              I Love Shopping
            </span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden lg:flex items-center space-x-8">
            <Link
              to="/products"
              className="text-gray-700 hover:text-primary-600 font-medium"
            >
              All Products
            </Link>
            <Link
              to="/category/electronics"
              className="text-gray-700 hover:text-primary-600 font-medium"
            >
              Electronics
            </Link>
            <Link
              to="/category/clothing"
              className="text-gray-700 hover:text-primary-600 font-medium"
            >
              Clothing
            </Link>
            <Link
              to="/category/home"
              className="text-gray-700 hover:text-primary-600 font-medium"
            >
              Home & Garden
            </Link>
          </nav>

          {/* Search bar - Desktop */}
          <div className="hidden md:block flex-1 max-w-md mx-8">
            <SearchBar />
          </div>

          {/* Actions */}
          <div className="flex items-center space-x-4">
            {/* Search toggle - Mobile */}
            <button
              onClick={() => setIsSearchOpen(!isSearchOpen)}
              className="md:hidden p-2 text-gray-600 hover:text-primary-600"
            >
              <Search className="h-6 w-6" />
            </button>

            {/* User menu */}
            {isAuthenticated ? (
              <div className="relative group">
                <button className="flex items-center space-x-2 p-2 text-gray-600 hover:text-primary-600">
                  <User className="h-6 w-6" />
                  <span className="hidden lg:inline">{user?.firstName}</span>
                </button>
                <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all">
                  <Link
                    to="/profile"
                    className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                  >
                    My Profile
                  </Link>
                  <Link
                    to="/orders"
                    className="block px-4 py-2 text-gray-700 hover:bg-gray-100"
                  >
                    My Orders
                  </Link>
                  <hr />
                  <button
                    onClick={logout}
                    className="block w-full text-left px-4 py-2 text-gray-700 hover:bg-gray-100"
                  >
                    Logout
                  </button>
                </div>
              </div>
            ) : (
              <Link
                to="/login"
                className="flex items-center space-x-2 p-2 text-gray-600 hover:text-primary-600"
              >
                <User className="h-6 w-6" />
                <span className="hidden lg:inline">Login</span>
              </Link>
            )}

            {/* Cart */}
            <Link
              to="/cart"
              className="relative p-2 text-gray-600 hover:text-primary-600"
            >
              <ShoppingCart className="h-6 w-6" />
              <span className="absolute -top-1 -right-1 bg-primary-600 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                0
              </span>
            </Link>

            {/* Mobile menu toggle */}
            <button
              onClick={() => setIsMenuOpen(!isMenuOpen)}
              className="lg:hidden p-2 text-gray-600 hover:text-primary-600"
            >
              {isMenuOpen ? (
                <X className="h-6 w-6" />
              ) : (
                <Menu className="h-6 w-6" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile search */}
        {isSearchOpen && (
          <div className="md:hidden py-4 border-t">
            <SearchBar />
          </div>
        )}

        {/* Mobile menu */}
        {isMenuOpen && (
          <nav className="lg:hidden py-4 border-t">
            <Link
              to="/products"
              className="block py-2 text-gray-700 hover:text-primary-600"
              onClick={() => setIsMenuOpen(false)}
            >
              All Products
            </Link>
            <Link
              to="/category/electronics"
              className="block py-2 text-gray-700 hover:text-primary-600"
              onClick={() => setIsMenuOpen(false)}
            >
              Electronics
            </Link>
            <Link
              to="/category/clothing"
              className="block py-2 text-gray-700 hover:text-primary-600"
              onClick={() => setIsMenuOpen(false)}
            >
              Clothing
            </Link>
            <Link
              to="/category/home"
              className="block py-2 text-gray-700 hover:text-primary-600"
              onClick={() => setIsMenuOpen(false)}
            >
              Home & Garden
            </Link>
          </nav>
        )}
      </div>
    </header>
  );
}
