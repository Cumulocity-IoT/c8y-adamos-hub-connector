package com.adamos.hubconnector.model.hub;

import com.adamos.hubconnector.model.TreeType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode(callSuper=true)
public class TreeDTO extends MDMObjectDTO{
	private boolean defaultTree;
	private NodeDTO root;
	private TreeType treeType;
	private int version;
	
}
