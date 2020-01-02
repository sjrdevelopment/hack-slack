const fetch = require('node-fetch')
var get = require('lodash/get')
const {Storage} = require('@google-cloud/storage')
const fs = require('fs')
const {token} = require('../secret.js')

const getHistoryUrl = 'https://slack.com/api/conversations.history'
const getChannelsUrl = 'https://slack.com/api/conversations.list'

const storage = new Storage()
const bucketName = 'hack_slack'


async function uploadChannelFile(data, channel) {
    const file = storage.bucket(bucketName).file(`message-data/${channel}.json`);

    console.log('uploading file')
    await file.save(data, function(err) {
        if (!err) {
            console.log('file written directly')
        } else {
            console.log('error found:')
            console.log(err)
        }
    });
}

const processChannelMessages = async (messages, channel) => {
    console.log('processing messages for channel')
    const allMessagesText = messages.map(message => {
        return {
            timestamp: message.ts,
            text: message.text,
            channel: channel.name,
        }
    })

    let data = JSON.stringify(allMessagesText, null, 2);

    await uploadChannelFile(data, channel.name)
}

const fetchMessagesForChannel = async channel => {
    console.log('getting messages for channel')

    await fetch(`${getHistoryUrl}?token=${token}&channel=${channel.id}`)
        .then(res => res.json())
        .then(json => {
            if (json.messages) {
                processChannelMessages(json.messages, channel)
            } else {
                console.log('No Slack data')
            }

    });
}

const processSlackChannels = async data => {
    if (data.channels) {
        const channels = data.channels.map(channel => {
            return {
                id: channel.id,
                name: channel.name,
            }
        })

        console.log(channels)

        await channels.forEach(channel => fetchMessagesForChannel(channel))

        
  
    } else {
        console.log('no channels found')
    }
}

const fetchSlackMessages = async () => {
    await fetch(`${getChannelsUrl}?token=${token}`)
        .then(res => res.json())
        .then(json => processSlackChannels(json))

}

exports.fetchSlackMessages = fetchSlackMessages
