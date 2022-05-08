export interface IEventTrigger {
    eventCode: string;
    referenceObjectType: string;
    referenceObjectIds: Array<string>;
    devices: Array<string>;
}
