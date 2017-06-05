# Molnet_IOT

Hello Folks,

In this project we are planning to collect data from Molenet model through the RF wireless receiver. The Molnet  is burried under the soil  and sensors attached to the molenet sense the data such as temprature,water content etc. This data is being transmitted by the Molnet module in RF frequncy to the outside world.

Our vision is to collect this data most cost effective way(in terms of energy cost, user-friendliness, complexity in implementation etc.). Also plot or display the data in end device (tablet or mobile).


These are the task to be done in coming days
1. From Hardware/Adruino point of view.
At the sender side:
Create a 23 byte packet encoder and send it in the interval specified in evaluation document.

At the Reciever side:
i)Recieve the 23 byte and extract necessary data
ii) Store these data in local database
iii)optimize power or energy consumptions using SLEEP processor or SLEEP radio.
iv)send the data to app in a popular format-may be json

2. From Hardware/Adruino point of view.

i) Combine the two graphs and improve the layout
ii)Check how to plot real time data
iii)Local database to store the data/send data to webserver.
iv) Test the app
