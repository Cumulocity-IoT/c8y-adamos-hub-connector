package com.adamos.hubconnector.model.hub;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
@EqualsAndHashCode(callSuper=true)
public class NodeDTO extends MDMObjectDTO{
	private List<MDMObjectDTO> children;
	private ReferencedEntityDTO referencedEntity;
}
