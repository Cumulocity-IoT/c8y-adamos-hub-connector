package com.adamos.hubconnector.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class RestPage<T> implements Page<T> {
    private PageImpl<T> pageDelegate = new PageImpl<>(new ArrayList<>(0));

    public List<T> getContent() {
        return pageDelegate.getContent();
    }

    public int getNumber() {
        return pageDelegate.getNumber();
    }

    public int getNumberOfElements() {
        return pageDelegate.getNumberOfElements();
    }

    public int getSize() {
        return pageDelegate.getSize();
    }

    public Sort getSort() {
        return pageDelegate.getSort();
    }

    public long getTotalElements() {
        return pageDelegate.getTotalElements();
    }

    public int getTotalPages() {
        return pageDelegate.getTotalPages();
    }

    public boolean hasContent() {
        return pageDelegate.hasContent();
    }

    public boolean hasNext() {
        return pageDelegate.hasNext();
    }

    public boolean hasPrevious() {
        return pageDelegate.hasPrevious();
    }

    public boolean isFirst() {
        return pageDelegate.isFirst();
    }

    public boolean isLast() {
        return pageDelegate.isLast();
    }

    public Iterator<T> iterator() {
        return pageDelegate.iterator();
    }

	@Override
	public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return pageDelegate.map(converter);
	}

    public Pageable nextPageable() {
        return pageDelegate.nextPageable();
    }

    public Pageable previousPageable() {
        return pageDelegate.previousPageable();
    }

    public void setContent(List<T> content) {
        pageDelegate = new PageImpl<>(content, null, getTotalElements());
    }


    public void setTotalElements(int totalElements) {
        pageDelegate = new PageImpl<>(getContent(), null, totalElements);
    }

    public String toString() {
        return pageDelegate.toString();
    }

}