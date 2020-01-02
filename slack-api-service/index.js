const fetchSlackMessages = require('./server').fetchSlackMessages

exports.extractSlackMessages = (req, res) => {
    const response = fetchSlackMessages()
    console.log(response)
    res && res.status(200).send(response);
  };
