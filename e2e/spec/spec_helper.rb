require "spec_helper"
require "faker"
require "net/http"
require "net/smtp"
require "oga"
require "pry"
require "dotenv"
require "mail"

Dotenv.load

MAILCATCHER_SMTP_URL = ENV.fetch("MAILCATCHER_SMTP_URL", "localhost:3025")
MAILCATCHER_HTTP_URL = ENV.fetch("MAILCATCHER_HTTP_URL", "localhost:8080")

MAILCATCHER_SMTP_HOST = MAILCATCHER_SMTP_URL.split(":").first
MAILCATCHER_SMTP_PORT = MAILCATCHER_SMTP_URL.split(":").last.to_i

MAILCATCHER_HTTP_HOST = MAILCATCHER_HTTP_URL.split(":").first
MAILCATCHER_HTTP_PORT = MAILCATCHER_HTTP_URL.split(":").last.to_i

Dir.glob(File.join(File.dirname(__FILE__), "support", "*.rb")).each do |file|
  require file
end

RSpec.configure do |config|
  config.filter_run focus: true
  config.run_all_when_everything_filtered = true
end