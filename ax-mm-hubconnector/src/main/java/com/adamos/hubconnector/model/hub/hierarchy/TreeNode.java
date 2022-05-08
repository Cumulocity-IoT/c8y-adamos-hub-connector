package com.adamos.hubconnector.model.hub.hierarchy;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TreeNode<T> {

	public T data;
	
	@JsonIgnore
	public TreeNode<T> parent;

	public List<TreeNode<T>> children;

	@JsonIgnore
	public boolean isRoot() {
		return parent == null;
	}

	@JsonIgnore
	public boolean isLeaf() {
		return children.size() == 0;
	}

	@JsonIgnore
	private List<TreeNode<T>> elementsIndex;

	public TreeNode(T data) {
		this.data = data;
		this.children = new LinkedList<TreeNode<T>>();
		this.elementsIndex = new LinkedList<TreeNode<T>>();
		this.elementsIndex.add(this);
	}

	public TreeNode<T> addChild(T child) {
		TreeNode<T> childNode = new TreeNode<T>(child);
		childNode.parent = this;
		this.children.add(childNode);
		this.registerChildForSearch(childNode);
		return childNode;
	}

	public int getLevel() {
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}

	private void registerChildForSearch(TreeNode<T> node) {
		elementsIndex.add(node);
		if (parent != null)
			parent.registerChildForSearch(node);
	}

	public TreeNode<T> findTreeNode(Comparable<T> cmp) {
		for (TreeNode<T> element : this.elementsIndex) {
			T elData = element.data;
			if (cmp.compareTo(elData) == 0)
				return element;
		}

		return null;
	}

	@Override
	public String toString() {
		return data != null ? data.toString() : "[data null]";
	}

}