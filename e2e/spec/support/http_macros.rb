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
end
