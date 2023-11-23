module HttpMacros
  def clear_all
    http do |http|
      http.delete("/messages")
    end
  end

  def http
    Net::HTTP.start(MAILCATCHER_HTTP_HOST, MAILCATCHER_HTTP_PORT) do |http|
      yield http
    end
  end

  def default_404_html
    message = "<html>\n<body>\n  <h1>No Dice</h1>\n  <p>The message you were looking for does not exist, or doesn't have content of this type.</p>\n</body>\n</html>"
    message.strip + "\n"
  end
end
