export interface IHubResponse<T> {
    id: string;
    name: string;
    data: T;
}