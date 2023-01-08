# emoncms Persistence service for openHAB

Update 23/11/15 : you can now set the configuration line "sendinterval" (see below)
The send interval is the time the binding will wait, when receiving a data to store, before sending the request to the server.
Each new store request during this interval will be added to make only one request to the server and reduce greatly the number of request and the time.
This is very useful when persisting big group (tested with 1300+ items) with time based persistence strategies (everyMinute, everyHour...).
If set to 0, each store request will result in an instant request to the server (better for event based persistence strategies) 


This binding provides a persistence service for emoncms (http://emoncms.org/).
You can use it with the official server or your own.


## Installation

Compile with Maven then drop the .jar file  folder in your openHAB `addons` directory.

Additionally, place a persistence file called emoncms.persist in the `${openhab.home}/configuration/persistence` folder.

## Configuration

This persistence service can be configured in `openhab.cfg`.
The configuration should look like this : 

>######################## Emoncms Persistence Service ##############################
>\#
>\# the url of the emoncms server (optional, default is http://emoncms.org/)

> emoncms:url=

>\# the emoncms API-Key for authentication (mandatory, generated on the emoncms server or website)

> emoncms:apikey=

>\# the node number (optional, default is 0)

> emoncms:node=

>\# if the value should be rounded (optional, default is false)

> emoncms:round=

>\# the interval is ms between sends in order to store multiple object within one request
>\# default is 0, in this case each store request results in a request to the server

> emoncms:sendinterval=


All item and event related configuration is done in the emoncms.persist file. Aliases do not have any special meaning for the emoncms persistence service.



## Development

This is a simple dev, for a basic use. It only posts datas as inputs on the server.
Feeds and logs need to be created elsewhere.
