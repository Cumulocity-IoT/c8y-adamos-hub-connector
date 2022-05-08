import { IHubRule } from "./IHubRule";
// import { IAdamosRule } from "./IAdamosRule";
import { ICustomPayload } from "./ICustomPayload";

export interface IEventRule {
	eventTrigger: IHubRule;
	eventProcessor: any;
	// adamos: IAdamosRule;
	// payloadProcessingMode: string;
	output: string;
	// selectedAttributes: string[];
	id: string;
	name: string;
	enabled: boolean;
}