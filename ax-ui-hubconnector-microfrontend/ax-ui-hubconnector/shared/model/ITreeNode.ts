export interface ITreeNode<T> {
    data: T;
    parent: ITreeNode<T>;
    children: Array<ITreeNode<T>>;
    level: number;
}
