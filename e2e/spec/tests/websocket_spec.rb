require "spec_helper"

describe "Websocket" do
  include HttpMacros
  include SmtpMacros

  before :all do
    clear_all
    Celluloid.boot
    @websocket_url = "ws://#{MAILCATCHER_HTTP_HOST}:#{MAILCATCHER_HTTP_PORT}/messages"
  end

  after :all do
    clear_all
    Celluloid.shutdown
  end

  describe "text email" do
    before :all do
      @client = WebsocketClient.new @websocket_url
    end

    after :all do
      @client.close
    end

    before :all do
      @sender = Faker::Internet.email
      @recipient = Faker::Internet.email
      @subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      message = <<~MESSAGE
        From: #{@sender}
        To: #{@recipient}
        Subject: #{@subject}

        #{@body}
      MESSAGE
      @message = message.gsub("\n", "\r\n") # SMTP requires \r\n line
    end

    it "should receive message over websocket" do
      expect(@client.messages).to be_empty

      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end

      sleep 1

      expect(@client.messages.size).to eq 1

      message = @client.messages.first

      expect(message).to eq(
        {
          "type" => "add",
          "message" => {
            "id" => 1,
            "sender" => "<#{@sender}>",
            "recipients" => ["<#{@recipient}>"],
            "subject" => @subject,
            "size" => @message.size.to_s,
            "type" => "text/plain",
            "created_at" => message["message"]["created_at"]
          }
        }
      )
    end

    it "should send clear message over websocket" do
      expect(@client.messages.size).to eq 1

      clear_all
      sleep 1

      expect(@client.messages.size).to eq 2

      message = @client.messages.last

      expect(message).to eq(
        {
          "type" => "clear"
        }
      )
    end
  end

  describe "html email" do
    before :all do
      @client = WebsocketClient.new @websocket_url
    end

    after :all do
      @client.close
    end

    before :all do
      sender = Faker::Internet.email
      recipient = Faker::Internet.email
      subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      @sender = sender
      @recipient = recipient
      @subject = subject

      @message = Mail.deliver do
        to recipient
        from sender
        subject subject

        html_part do
          content_type "text/html; charset=UTF-8"
          body "<h1>This is HTML</h1>"
        end
      end.to_s
    end

    it "should receive message over websocket" do
      expect(@client.messages).to be_empty

      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end

      sleep 1

      expect(@client.messages.size).to eq 1

      message = @client.messages.first

      expect(message).to eq(
        {
          "type" => "add",
          "message" => {
            "id" => 1,
            "sender" => "<#{@sender}>",
            "recipients" => ["<#{@recipient}>"],
            "subject" => @subject,
            "size" => @message.size.to_s,
            "type" => "multipart/mixed",
            "created_at" => message["message"]["created_at"]
          }
        }
      )
    end

    it "should send clear message over websocket" do
      expect(@client.messages.size).to eq 1

      clear_all
      sleep 1

      expect(@client.messages.size).to eq 2

      message = @client.messages.last

      expect(message).to eq(
        {
          "type" => "clear"
        }
      )
    end
  end

  describe "html + text email" do
    before :all do
      @client = WebsocketClient.new @websocket_url
    end

    after :all do
      @client.close
    end

    before :all do
      sender = Faker::Internet.email
      recipient = Faker::Internet.email
      subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      @sender = sender
      @recipient = recipient
      @subject = subject

      @message = Mail.deliver do
        to recipient
        from sender
        subject subject

        text_part do
          body "This is plain text"
        end

        html_part do
          content_type "text/html; charset=UTF-8"
          body "<h1>This is HTML</h1>"
        end
      end.to_s
    end

    it "should receive message over websocket" do
      expect(@client.messages).to be_empty

      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end

      sleep 1

      expect(@client.messages.size).to eq 1

      message = @client.messages.first

      expect(message).to eq(
        {
          "type" => "add",
          "message" => {
            "id" => 1,
            "sender" => "<#{@sender}>",
            "recipients" => ["<#{@recipient}>"],
            "subject" => @subject,
            "size" => @message.size.to_s,
            "type" => "multipart/alternative",
            "created_at" => message["message"]["created_at"]
          }
        }
      )
    end

    it "should send clear message over websocket" do
      expect(@client.messages.size).to eq 1

      clear_all
      sleep 1

      expect(@client.messages.size).to eq 2

      message = @client.messages.last

      expect(message).to eq(
        {
          "type" => "clear"
        }
      )
    end
  end
end
