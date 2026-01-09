import { Link } from 'react-router-dom';
import { Heart, ShoppingCart, Star } from 'lucide-react';
import type { Product } from '@/types';
import clsx from 'clsx';

interface ProductCardProps {
  product: Product;
}

export default function ProductCard({ product }: ProductCardProps) {
  const {
    slug,
    name,
    primaryImageUrl,
    price,
    compareAtPrice,
    averageRating,
    reviewCount,
    inStock,
    onSale,
    discountPercentage,
    featured,
  } = product;

  return (
    <div className="card group">
      {/* Image */}
      <Link to={`/products/${slug}`} className="block relative aspect-square overflow-hidden">
        <img
          src={primaryImageUrl || '/placeholder.jpg'}
          alt={name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
        
        {/* Badges */}
        <div className="absolute top-3 left-3 flex flex-col gap-2">
          {onSale && (
            <span className="badge-sale">
              -{discountPercentage?.toFixed(0)}%
            </span>
          )}
          {featured && <span className="badge-featured">Featured</span>}
          {!inStock && (
            <span className="badge bg-gray-800 text-white">Out of Stock</span>
          )}
        </div>

        {/* Quick actions */}
        <div className="absolute top-3 right-3 flex flex-col gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
          <button className="p-2 bg-white rounded-full shadow-md hover:bg-primary-50 hover:text-primary-600 transition-colors">
            <Heart className="h-5 w-5" />
          </button>
        </div>

        {/* Add to cart overlay */}
        <div className="absolute bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-black/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity">
          <button
            disabled={!inStock}
            className={clsx(
              'w-full py-2.5 rounded-lg font-medium flex items-center justify-center space-x-2 transition-colors',
              inStock
                ? 'bg-white text-gray-900 hover:bg-primary-600 hover:text-white'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            )}
          >
            <ShoppingCart className="h-5 w-5" />
            <span>{inStock ? 'Add to Cart' : 'Out of Stock'}</span>
          </button>
        </div>
      </Link>

      {/* Content */}
      <div className="p-4">
        <Link to={`/products/${slug}`}>
          <h3 className="font-medium text-gray-900 hover:text-primary-600 line-clamp-2 min-h-[48px]">
            {name}
          </h3>
        </Link>

        {/* Rating */}
        <div className="flex items-center mt-2 space-x-1">
          <div className="flex items-center">
            {[1, 2, 3, 4, 5].map((star) => (
              <Star
                key={star}
                className={clsx(
                  'h-4 w-4',
                  star <= Math.round(averageRating)
                    ? 'text-yellow-400 fill-yellow-400'
                    : 'text-gray-300'
                )}
              />
            ))}
          </div>
          <span className="text-sm text-gray-500">({reviewCount})</span>
        </div>

        {/* Price */}
        <div className="mt-2 flex items-center space-x-2">
          <span className="text-lg font-bold text-gray-900">
            ${price.toFixed(2)}
          </span>
          {compareAtPrice && compareAtPrice > price && (
            <span className="text-sm text-gray-500 line-through">
              ${compareAtPrice.toFixed(2)}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
