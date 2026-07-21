fetch('http://localhost:8080/api/v1/chat/stream', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ message: 'Say "hello world" in 5 words.' })
}).then(async res => {
  const reader = res.body.getReader();
  const decoder = new TextDecoder();
  while (true) {
    const {value, done} = await reader.read();
    if (done) break;
    console.log(JSON.stringify(decoder.decode(value)));
  }
});
