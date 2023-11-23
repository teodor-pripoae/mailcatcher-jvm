require "celluloid/websocket/client"

class WebsocketClient
  include Celluloid

  def initialize(url)
    @client = Celluloid::WebSocket::Client.new(url, current_actor)
    @counter = 0
    @messages = []
  end

  def on_open
    # puts("websocket connection opened")
  end

  def on_message(data)
    @counter += 1
    message = JSON.parse(data)
    # puts("websocket message received: #{message.inspect}")
    messages << message
  end

  def on_close(code, reason)
    # puts("websocket connection closed: #{code.inspect}, #{reason.inspect}")
  end

  def close
    @client.close
  end

  attr_reader :messages
end
