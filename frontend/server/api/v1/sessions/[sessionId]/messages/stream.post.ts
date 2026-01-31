import { defineEventHandler, getRouterParam, readBody } from 'h3';

export default defineEventHandler(async (event) => {
  const sessionId = getRouterParam(event, 'sessionId');
  const body = await readBody(event);

  const targetUrl = `http://localhost:8080/api/v1/sessions/${sessionId}/messages/stream`;

  const response = await fetch(targetUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
    body: JSON.stringify(body),
  });

  event.node.res.statusCode = response.status;
  event.node.res.statusMessage = response.statusText;

  const contentType = response.headers.get('content-type');
  if (contentType) {
    event.node.res.setHeader('Content-Type', contentType);
  }

  if (response.body) {
    const reader = response.body.getReader();

    try {
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        event.node.res.write(value);
      }
    } finally {
      event.node.res.end();
    }
  } else {
    event.node.res.end();
  }

  return;
});
