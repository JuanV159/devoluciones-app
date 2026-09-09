export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  detalle: string;
  path: string;
}
