"I pledge the highest level of ethical principles in support of academic excellence. I ensure that all of my work reflects my own abilities and not those of someone else."

We can add a pending intent and pass it to the SMSManager, that will send a broadcast so that the broadcastReceiver will get it.
This pending intent would actually check if sms was delivered or not (by the result code) and tell us via the broadcast, so we could update our message.
