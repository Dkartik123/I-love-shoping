import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { productApi } from '@/services/productApi';

export default function ProductDetailPage() {
  const { slug } = useParams<{ slug: string }>();

  const { data: product, isLoading } = useQuery({
    queryKey: ['product', slug],
    queryFn: () => productApi.getProductBySlug(slug!),
    enabled: !!slug,
  });

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="animate-pulse">
          <div className="grid md:grid-cols-2 gap-8">
            <div className="aspect-square bg-gray-200 rounded-lg" />
            <div className="space-y-4">
              <div className="h-8 bg-gray-200 rounded w-3/4" />
              <div className="h-6 bg-gray-200 rounded w-1/4" />
              <div className="h-4 bg-gray-200 rounded w-full" />
              <div className="h-4 bg-gray-200 rounded w-5/6" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-center">
        <h1 className="text-2xl font-bold text-gray-900">Product not found</h1>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="grid md:grid-cols-2 gap-8">
        {/* Images */}
        <div>
          <img
            src={product.primaryImageUrl || '/placeholder.jpg'}
            alt={product.name}
            className="w-full aspect-square object-cover rounded-lg"
          />
        </div>

        {/* Details */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900">{product.name}</h1>
          
          <div className="mt-4 flex items-center gap-4">
            <span className="text-2xl font-bold text-gray-900">
              ${product.price.toFixed(2)}
            </span>
            {product.compareAtPrice && product.compareAtPrice > product.price && (
              <span className="text-lg text-gray-500 line-through">
                ${product.compareAtPrice.toFixed(2)}
              </span>
            )}
            {product.onSale && (
              <span className="badge-sale">
                Save {product.discountPercentage?.toFixed(0)}%
              </span>
            )}
          </div>

          <p className="mt-6 text-gray-600">{product.shortDescription}</p>

          <div className="mt-6">
            <span
              className={`inline-block px-3 py-1 rounded-full text-sm font-medium ${
                product.inStock
                  ? 'bg-green-100 text-green-800'
                  : 'bg-red-100 text-red-800'
              }`}
            >
              {product.inStock ? 'In Stock' : 'Out of Stock'}
            </span>
          </div>

          <button
            disabled={!product.inStock}
            className="mt-8 btn-primary w-full py-3 text-lg disabled:opacity-50"
          >
            {product.inStock ? 'Add to Cart' : 'Out of Stock'}
          </button>

          {/* Description */}
          {product.description && (
            <div className="mt-8">
              <h2 className="text-lg font-semibold mb-4">Description</h2>
              <div
                className="prose text-gray-600"
                dangerouslySetInnerHTML={{ __html: product.description }}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
