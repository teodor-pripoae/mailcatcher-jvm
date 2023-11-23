require "spec_helper"

describe "Text Email" do
  include HttpMacros
  include SmtpMacros

  before :all do
    clear_all
  end

  after :all do
    clear_all
  end

  describe "when sent to a single recipient using hardcoded string" do
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

    after :all do
      clear_all
    end

    it "should be delivered" do
      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end
    end

    it "should contain only one message" do
      response = http { |http| http.get("/messages") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Array
      expect(data.size).to eq 1
      message = data.first

      expect(message).to be_a Hash
      expect(message).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => message["created_at"]
        }
      )
    end

    it "should return json response" do
      response = http { |http| http.get("/messages/1.json") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Hash
      expect(data).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => data["created_at"],
          "attachments" => [],
          "formats" => ["source", "plain"],
          "type" => "text/plain"
        }
      )
    end

    it "should return plain content" do
      response = http { |http| http.get("/messages/1.plain") }
      expect(response.code).to eq "200"
      expect(response.content_type).to eq "text/plain"
      expect(response.body).to eq @body + "\n"
    end

    it "should return 404 for html content" do
      response = http { |http| http.get("/messages/1.html") }
      expect(response.code).to eq "404"
      expect(response.content_type).to eq "text/html"
      expect(response.body).to eq(default_404_html)
    end

    it "should return source content" do
      response = http { |http| http.get("/messages/1.source") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "text/plain"
    end

    it "should return eml content" do
      response = http { |http| http.get("/messages/1.eml") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "message/rfc822"
    end
  end

  describe "when sent to a single recipient using mail library" do
    before :all do
      @sender = Faker::Internet.email
      @recipient = Faker::Internet.email
      @subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      @message = new_email(
        sender: @sender,
        recipient: @recipient,
        subject: @subject,
        body_plain: @body,
      )
    end

    after :all do
      clear_all
    end

    it "should be delivered" do
      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end
    end

    it "should contain only one message" do
      response = http { |http| http.get("/messages") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Array
      expect(data.size).to eq 1
      message = data.first

      expect(message).to be_a Hash
      expect(message).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => message["created_at"]
        }
      )
    end

    it "should return json response" do
      response = http { |http| http.get("/messages/1.json") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Hash
      expect(data).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => data["created_at"],
          "attachments" => [],
          "formats" => ["source", "plain"],
          "type" => "multipart/mixed"
        }
      )
    end

    it "should return plain content" do
      response = http { |http| http.get("/messages/1.plain") }
      expect(response.code).to eq "200"
      expect(response.content_type).to eq "text/plain"
      expect(response.body).to eq @body
    end

    it "should return 404 for html content" do
      response = http { |http| http.get("/messages/1.html") }
      expect(response.code).to eq "404"
      expect(response.content_type).to eq "text/html"
      expect(response.body).to eq(default_404_html)
    end

    it "should return source content" do
      response = http { |http| http.get("/messages/1.source") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "text/plain"
    end

    it "should return eml content" do
      response = http { |http| http.get("/messages/1.eml") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "message/rfc822"
    end
  end

  describe "when sent to a single recipient using mail library with sender/recipient name" do
    before :all do
      @sender = Faker::Internet.email
      @sender_name = Faker::Name.name
      @recipient = Faker::Internet.email
      @recipient_name = Faker::Name.name
      @subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      @message = new_email(
        sender: "#{@sender_name} <#{@sender}>",
        recipient: "#{@recipient_name} <#{@recipient}>",
        subject: @subject,
        body_plain: @body,
      )
    end

    after :all do
      clear_all
    end

    it "should be delivered" do
      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end
    end

    it "should contain only one message" do
      response = http { |http| http.get("/messages") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Array
      expect(data.size).to eq 1
      message = data.first

      expect(message).to be_a Hash
      expect(message).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => message["created_at"]
        }
      )
    end

    it "should return json response" do
      response = http { |http| http.get("/messages/1.json") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Hash
      expect(data).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => data["created_at"],
          "attachments" => [],
          "formats" => ["source", "plain"],
          "type" => "multipart/mixed"
        }
      )
    end

    it "should return plain content" do
      response = http { |http| http.get("/messages/1.plain") }
      expect(response.code).to eq "200"
      expect(response.content_type).to eq "text/plain"
      expect(response.body).to eq @body
    end

    it "should return 404 for html content" do
      response = http { |http| http.get("/messages/1.html") }
      expect(response.code).to eq "404"
      expect(response.content_type).to eq "text/html"
      expect(response.body).to eq(default_404_html)
    end

    it "should return source content" do
      response = http { |http| http.get("/messages/1.source") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "text/plain"
    end

    it "should return eml content" do
      response = http { |http| http.get("/messages/1.eml") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "message/rfc822"
    end
  end

  describe "when sent to a single recipient using mail library with sender/recipient name as list" do
    before :all do
      @sender = Faker::Internet.email
      @sender_name = Faker::Name.name
      @recipient = Faker::Internet.email
      @recipient_name = Faker::Name.name
      @subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      @message = new_email(
        sender: "#{@sender_name} <#{@sender}>",
        recipient: ["#{@recipient_name} <#{@recipient}>"],
        subject: @subject,
        body_plain: @body,
      )
    end

    after :all do
      clear_all
    end

    it "should be delivered" do
      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end
    end

    it "should contain only one message" do
      response = http { |http| http.get("/messages") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Array
      expect(data.size).to eq 1
      message = data.first

      expect(message).to be_a Hash
      expect(message).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => message["created_at"]
        }
      )
    end

    it "should return json response" do
      response = http { |http| http.get("/messages/1.json") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Hash
      expect(data).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => data["created_at"],
          "attachments" => [],
          "formats" => ["source", "plain"],
          "type" => "multipart/mixed"
        }
      )
    end

    it "should return plain content" do
      response = http { |http| http.get("/messages/1.plain") }
      expect(response.code).to eq "200"
      expect(response.content_type).to eq "text/plain"
      expect(response.body).to eq @body
    end

    it "should return 404 for html content" do
      response = http { |http| http.get("/messages/1.html") }
      expect(response.code).to eq "404"
      expect(response.content_type).to eq "text/html"
      expect(response.body).to eq(default_404_html)
    end

    it "should return source content" do
      response = http { |http| http.get("/messages/1.source") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "text/plain"
    end

    it "should return eml content" do
      response = http { |http| http.get("/messages/1.eml") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "message/rfc822"
    end
  end

  describe "when sent to multiple recipients using mail library with sender/recipient name" do
    before :all do
      @sender = Faker::Internet.email
      @sender_name = Faker::Name.name
      @recipient1 = Faker::Internet.email
      @recipient1_name = Faker::Name.name
      @recipient2 = Faker::Internet.email
      @recipient2_name = Faker::Name.name
      @recipient3 = Faker::Internet.email
      @recipient3_name = Faker::Name.name
      @subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      @recipients = [
          "#{@recipient1_name} <#{@recipient1}>",
          "#{@recipient2_name} <#{@recipient2}>",
          "#{@recipient3_name} <#{@recipient3}>",
      ]

      @message = new_email(
        sender: "#{@sender_name} <#{@sender}>",
        recipient: @recipients,
        subject: @subject,
        body_plain: @body,
      )
    end

    after :all do
      clear_all
    end

    it "should be delivered" do
      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipients
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end
    end

    it "should contain only one message" do
      response = http { |http| http.get("/messages") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Array
      expect(data.size).to eq 1
      message = data.first

      expect(message).to be_a Hash
      expect(message).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipients[0]}>", "<#{@recipients[1]}>", "<#{@recipients[2]}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => message["created_at"]
        }
      )
    end

    it "should return json response" do
      response = http { |http| http.get("/messages/1.json") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Hash
      expect(data).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipients[0]}>", "<#{@recipients[1]}>", "<#{@recipients[2]}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => data["created_at"],
          "attachments" => [],
          "formats" => ["source", "plain"],
          "type" => "multipart/mixed"
        }
      )
    end

    it "should return plain content" do
      response = http { |http| http.get("/messages/1.plain") }
      expect(response.code).to eq "200"
      expect(response.content_type).to eq "text/plain"
      expect(response.body).to eq @body
    end

    it "should return 404 for html content" do
      response = http { |http| http.get("/messages/1.html") }
      expect(response.code).to eq "404"
      expect(response.content_type).to eq "text/html"
      expect(response.body).to eq(default_404_html)
    end

    it "should return source content" do
      response = http { |http| http.get("/messages/1.source") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "text/plain"
    end

    it "should return eml content" do
      response = http { |http| http.get("/messages/1.eml") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "message/rfc822"
    end
  end

  describe "when sent using attachments" do
    before :all do
      @sender = Faker::Internet.email
      @recipient = Faker::Internet.email
      @subject = Faker::Lorem.sentence
      @body = Faker::Lorem.paragraph

      @message = new_email(
        sender: @sender,
        recipient: @recipient,
        subject: @subject,
        body_plain: @body,
        body_html: nil,
        attachments: [
          File.join(File.dirname(__FILE__), "..", "fixtures", "attachment.txt"),
          File.join(File.dirname(__FILE__), "..", "fixtures", "image1.jpg")
        ]
      )
    end

    it "should be delivered" do
      smtp do |smtp|
        response = smtp.send_message @message, @sender, @recipient
        expect(response).to be_a Net::SMTP::Response
        expect(response.status).to eq "250"
      end
    end

    it "should contain only one message" do
      response = http { |http| http.get("/messages") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Array
      expect(data.size).to eq 1
      message = data.first

      expect(message).to be_a Hash
      expect(message).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => message["created_at"]
        }
      )
    end

    it "should return json response" do
      response = http { |http| http.get("/messages/1.json") }
      expect(response.code).to eq "200"
      data = JSON.parse(response.body)

      expect(data).to be_a Hash
      expect(data).to eq(
        {
          "id" => 1,
          "sender" => "<#{@sender}>",
          "recipients" => ["<#{@recipient}>"],
          "subject" => @subject,
          "size" => @message.size.to_s,
          "created_at" => data["created_at"],
          "formats" => ["source", "plain"],
          "type" => "multipart/mixed",
          "attachments" => data["attachments"],
        }
      )

      expect(data["attachments"]).to be_a Array
      expect(data["attachments"].size).to eq 2

      attachment = data["attachments"].first
      expect(attachment).to be_a Hash
      expect(attachment["filename"]).to eq "attachment.txt"
      expect(attachment["type"]).to eq "text/plain"
      expect(attachment["size"]).to eq 32

      attachment = data["attachments"].last
      expect(attachment).to be_a Hash
      expect(attachment["filename"]).to eq "image1.jpg"
      expect(attachment["type"]).to eq "image/jpeg"
      expect(attachment["size"]).to eq 25584
    end

    it "should return plain content" do
      response = http { |http| http.get("/messages/1.plain") }
      expect(response.code).to eq "200"
      expect(response.content_type).to eq "text/plain"
      expect(response.body).to eq @body
    end

    it "should return 404 for html content" do
      response = http { |http| http.get("/messages/1.html") }
      expect(response.code).to eq "404"
      expect(response.content_type).to eq "text/html"
      expect(response.body).to eq(default_404_html)
    end

    it "should return source content" do
      response = http { |http| http.get("/messages/1.source") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "text/plain"
    end

    it "should return eml content" do
      response = http { |http| http.get("/messages/1.eml") }
      expect(response.code).to eq "200"
      expect(response.body).to eq @message
      expect(response.content_type).to eq "message/rfc822"
    end
  end
end
