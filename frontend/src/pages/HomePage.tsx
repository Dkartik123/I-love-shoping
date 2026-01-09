import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowRight, Truck, Shield, RefreshCw, HeadphonesIcon } from 'lucide-react';
import { productApi } from '@/services/productApi';
import { categoryApi } from '@/services/categoryApi';
import ProductGrid from '@/components/products/ProductGrid';

export default function HomePage() {
  const { data: featuredProducts, isLoading: loadingFeatured } = useQuery({
    queryKey: ['products', 'featured'],
    queryFn: () => productApi.getFeaturedProducts(),
  });

  const { data: newArrivals, isLoading: loadingNew } = useQuery({
    queryKey: ['products', 'new-arrivals'],
    queryFn: () => productApi.getNewArrivals(),
  });

  const { data: onSale, isLoading: loadingSale } = useQuery({
    queryKey: ['products', 'on-sale'],
    queryFn: () => productApi.getOnSaleProducts(),
  });

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryApi.getCategories(),
  });

  return (
    <div>
      {/* Hero Section */}
      <section className="relative bg-gradient-to-r from-primary-600 to-primary-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 lg:py-32">
          <div className="max-w-2xl">
            <h1 className="text-4xl lg:text-6xl font-bold mb-6">
              Discover Products You'll Love
            </h1>
            <p className="text-lg lg:text-xl text-primary-100 mb-8">
              Shop the latest trends with amazing deals. Quality products,
              exceptional service, and free shipping on orders over $50.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Link
                to="/products"
                className="btn bg-white text-primary-600 hover:bg-primary-50 px-8 py-3 text-lg"
              >
                Shop Now
                <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
              <Link
                to="/products?onSale=true"
                className="btn border-2 border-white text-white hover:bg-white hover:text-primary-600 px-8 py-3 text-lg"
              >
                View Deals
              </Link>
            </div>
          </div>
        </div>
        <div className="absolute inset-0 bg-black/10" />
      </section>

      {/* Features */}
      <section className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-8">
            <div className="flex items-center space-x-3">
              <Truck className="h-10 w-10 text-primary-600" />
              <div>
                <h3 className="font-semibold">Free Shipping</h3>
                <p className="text-sm text-gray-500">On orders over $50</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <Shield className="h-10 w-10 text-primary-600" />
              <div>
                <h3 className="font-semibold">Secure Payment</h3>
                <p className="text-sm text-gray-500">100% secure checkout</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <RefreshCw className="h-10 w-10 text-primary-600" />
              <div>
                <h3 className="font-semibold">Easy Returns</h3>
                <p className="text-sm text-gray-500">30-day return policy</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <HeadphonesIcon className="h-10 w-10 text-primary-600" />
              <div>
                <h3 className="font-semibold">24/7 Support</h3>
                <p className="text-sm text-gray-500">Dedicated support</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Categories */}
      {categories && categories.length > 0 && (
        <section className="py-16 bg-gray-50">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between mb-8">
              <h2 className="text-2xl lg:text-3xl font-bold">Shop by Category</h2>
              <Link
                to="/categories"
                className="text-primary-600 hover:underline font-medium"
              >
                View All
              </Link>
            </div>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
              {categories.slice(0, 6).map((category) => (
                <Link
                  key={category.id}
                  to={`/category/${category.slug}`}
                  className="bg-white rounded-xl p-6 text-center hover:shadow-lg transition-shadow"
                >
                  <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <img
                      src={category.imageUrl || '/category-placeholder.png'}
                      alt={category.name}
                      className="w-10 h-10 object-contain"
                    />
                  </div>
                  <h3 className="font-medium text-gray-900">{category.name}</h3>
                  <p className="text-sm text-gray-500">
                    {category.productCount} products
                  </p>
                </Link>
              ))}
            </div>
          </div>
        </section>
      )}

      {/* Featured Products */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl lg:text-3xl font-bold">Featured Products</h2>
            <Link
              to="/products?featured=true"
              className="text-primary-600 hover:underline font-medium"
            >
              View All
            </Link>
          </div>
          <ProductGrid
            products={featuredProducts?.content || []}
            isLoading={loadingFeatured}
          />
        </div>
      </section>

      {/* Sale Banner */}
      <section className="py-16 bg-gradient-to-r from-red-600 to-red-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl lg:text-4xl font-bold mb-4">
            Mega Sale - Up to 50% Off!
          </h2>
          <p className="text-lg text-red-100 mb-8">
            Limited time offer on selected items. Don't miss out!
          </p>
          <Link
            to="/products?onSale=true"
            className="btn bg-white text-red-600 hover:bg-red-50 px-8 py-3 text-lg"
          >
            Shop Sale
          </Link>
        </div>
      </section>

      {/* New Arrivals */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl lg:text-3xl font-bold">New Arrivals</h2>
            <Link
              to="/products?new=true"
              className="text-primary-600 hover:underline font-medium"
            >
              View All
            </Link>
          </div>
          <ProductGrid
            products={newArrivals?.content || []}
            isLoading={loadingNew}
          />
        </div>
      </section>

      {/* On Sale */}
      <section className="py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-2xl lg:text-3xl font-bold">On Sale</h2>
            <Link
              to="/products?onSale=true"
              className="text-primary-600 hover:underline font-medium"
            >
              View All
            </Link>
          </div>
          <ProductGrid
            products={onSale?.content || []}
            isLoading={loadingSale}
          />
        </div>
      </section>

      {/* Newsletter */}
      <section className="py-16 bg-gray-900 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-2xl lg:text-3xl font-bold mb-4">
            Subscribe to Our Newsletter
          </h2>
          <p className="text-gray-400 mb-8 max-w-2xl mx-auto">
            Get the latest updates on new products, special offers, and exclusive
            deals delivered straight to your inbox.
          </p>
          <form className="flex flex-col sm:flex-row gap-4 max-w-md mx-auto">
            <input
              type="email"
              placeholder="Enter your email"
              className="flex-1 px-4 py-3 rounded-lg text-gray-900 focus:ring-2 focus:ring-primary-500"
            />
            <button
              type="submit"
              className="btn-primary px-8 py-3"
            >
              Subscribe
            </button>
          </form>
        </div>
      </section>
    </div>
  );
}
