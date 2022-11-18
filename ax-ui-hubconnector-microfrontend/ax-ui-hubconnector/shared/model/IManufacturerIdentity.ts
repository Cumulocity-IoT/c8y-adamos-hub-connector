import { IOwner } from "./IOwner";

export interface IManufacturerIdentity {
    adamosEcosystemId: string;
	adamosManufacturerId: string;
	manufacturerName: string;
	manufacturerSite: string;
	modelVersion: string;
	owner: IOwner;
	uniqueManufacturerPrefix: string;
	uuid: string;
	versionId: number;
}