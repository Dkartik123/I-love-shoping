// User types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  avatarUrl?: string;
  emailVerified: boolean;
  twoFactorEnabled: boolean;
  roles: string[];
  createdAt: string;
}

// Auth types
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
  requiresTwoFactor?: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
  twoFactorCode?: string;
  rememberMe?: boolean;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phone?: string;
  recaptchaToken: string;
}

export interface PasswordResetRequest {
  email: string;
}

export interface NewPasswordRequest {
  token: string;
  password: string;
  confirmPassword: string;
}

// Product types
export interface Product {
  id: string;
  sku: string;
  name: string;
  slug: string;
  shortDescription?: string;
  description?: string;
  price: number;
  compareAtPrice?: number;
  costPrice?: number;
  categoryId?: string;
  categoryName?: string;
  brandId?: string;
  brandName?: string;
  primaryImageUrl?: string;
  images: ProductImage[];
  attributes: ProductAttribute[];
  stockQuantity: number;
  inStock: boolean;
  lowStock: boolean;
  onSale: boolean;
  discountPercentage?: number;
  averageRating: number;
  reviewCount: number;
  featured: boolean;
  active: boolean;
  digital: boolean;
  weight?: {
    kg?: number;
    lb?: number;
  };
  dimensions?: {
    length?: number;
    width?: number;
    height?: number;
    unit: 'cm' | 'in';
  };
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductImage {
  id: string;
  imageUrl: string;
  altText?: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface ProductAttribute {
  name: string;
  value: string;
}

// Category types
export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  parentId?: string;
  children?: Category[];
  productCount: number;
  displayOrder: number;
  active: boolean;
}

// Brand types
export interface Brand {
  id: string;
  name: string;
  slug: string;
  description?: string;
  logoUrl?: string;
  websiteUrl?: string;
}

// Search and filter types
export interface ProductSearchParams {
  q?: string;
  categoryId?: string;
  brandId?: string;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  onSale?: boolean;
  minRating?: number;
  tags?: string[];
  sortBy?: 'name' | 'price' | 'rating' | 'createdAt' | 'soldCount';
  sortDir?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface FacetedSearchResult {
  products: Page<Product>;
  facets: SearchFacets;
}

export interface SearchFacets {
  categories: FacetItem[];
  brands: FacetItem[];
  priceRanges: FacetItem[];
  ratings: FacetItem[];
  attributes: AttributeFacet[];
}

export interface FacetItem {
  value: string;
  label: string;
  count: number;
}

export interface AttributeFacet {
  name: string;
  values: FacetItem[];
}

// Pagination types
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// API Response wrapper
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

// Error types
export interface ApiError {
  success: false;
  message: string;
  data?: Record<string, string>;
  timestamp: string;
}

// Review types
export interface ProductReview {
  id: string;
  productId: string;
  userId: string;
  userName: string;
  rating: number;
  title?: string;
  content?: string;
  verified: boolean;
  helpful: number;
  createdAt: string;
}
