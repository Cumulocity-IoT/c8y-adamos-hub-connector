package com.adamos.hubconnector.model.hub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class ImageDTO {
	private String uuid;
	private String lang;
	private String title;
	private String contentType;
	private String caption;
	private String imageSize;
	private String contentUrl;
}

//{
//    "uuid": "5cc1cac3e54ec9680d598dae",
//    "lang": "en",
//    "title": "DMF 600",
//    "contentType": "png",
//    "caption": "DMF 600",
//    "imageSize": "THUMBNAIL",
//    "contentUrl": "https://hubcatalogdev.blob.core.windows.net/dmg01/5cc1cac3e54ec9680d598dae?sv=2018-03-28&spr=https&se=2019-05-08T08%3A42%3A07Z&sr=b&sp=r&sig=2V2HOSr2aoSPwZ4BDtItbqNhf1qt5yUoxtP1kBtGxsk%3D"
//}