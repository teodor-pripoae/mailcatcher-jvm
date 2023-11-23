require "spec_helper"

describe "HTML Email" do
	include HttpMacros
	include SmtpMacros

	before :all do
		clear_all
	end

	after :all do
		clear_all
	end

	describe "when sent to a single recipient" do
		before :all do
			@sender = Faker::Internet.email
			@recipient = Faker::Internet.email
			@subject = Faker::Lorem.sentence
			@body = Faker::Lorem.paragraph

			sender = @sender
			recipient = @recipient
			subject = @subject

			@message = Mail.deliver do
				to recipient
				from sender
				subject subject

				html_part do
					content_type "text/html; charset=UTF-8"
					body "<html>\n<body>\n  <h1>This is HTML</h1>\n</body>\n</html>\n"
				end
			end.to_s
		end

		it "should be delivered" do
			smtp do |smtp|
				response = smtp.send_message @message, @sender, @recipient
				expect(response).to be_a Net::SMTP::Response
				expect(response.status).to eq "250"
			end
		end

		it "should contain only one message" do
			response = http { |http| http.get('/messages') }
			expect(response.code).to eq "200"
			data = JSON.parse(response.body)

			expect(data).to be_a Array
			expect(data.size).to eq 1
			message = data.first

			expect(message).to be_a Hash
			expect(message).to eq(
				{
					"id"=>1,
					"sender"=> "<#{@sender}>",
					"recipients" => ["<#{@recipient}>"],
					"subject" => @subject,
					"size"=> @message.size.to_s,
					"created_at"=> message["created_at"],
				}
			)
		end

		it "should return json response" do
			response = http { |http| http.get('/messages/1.json') }
			expect(response.code).to eq "200"
			data = JSON.parse(response.body)

			expect(data).to be_a Hash
			expect(data).to eq(
				{
					"id"=>1,
					"sender"=> "<#{@sender}>",
					"recipients" => ["<#{@recipient}>"],
					"subject" => @subject,
					"size"=> @message.size.to_s,
					"created_at"=> data["created_at"],
					"attachments" => [],
					"formats" => ["source", "html"],
					"type" => "multipart/mixed"
				}
			)
		end

		it "should return plain content" do
			response = http { |http| http.get('/messages/1.plain') }
			expect(response.code).to eq "404"
			expect(response.content_type).to eq "text/html"
			message = <<~MESSAGE
				<html>
        <body>
          <h1>No Dice</h1>
          <p>The message you were looking for does not exist, or doesn't have content of this type.</p>
        </body>
        </html>
				MESSAGE
			expect(response.body).to eq(message.strip() + "\n")
		end

		it "should return 200 for html content" do
			response = http { |http| http.get('/messages/1.html') }
			expect(response.code).to eq "200"
			expect(response.content_type).to eq "text/html"
			message = "<html>\n<body>\n  <h1>This is HTML</h1>\n</body>\n</html>\n"
			expect(response.body).to eq(message)
		end

		it "should return source content" do
			response = http { |http| http.get('/messages/1.source') }
			expect(response.code).to eq "200"
			expect(response.body).to eq @message
			expect(response.content_type).to eq "text/plain"
		end

		it "should return eml content" do
			response = http { |http| http.get('/messages/1.eml') }
			expect(response.code).to eq "200"
			expect(response.body).to eq @message
			expect(response.content_type).to eq "message/rfc822"
		end
	end
end