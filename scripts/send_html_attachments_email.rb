require "mail"

FROM_ADDRESS = "john@example.com"
FROM = "John Doe <#{FROM_ADDRESS}>"
TO = "test@example.com"

mail = Mail.deliver do
  to TO
  from FROM
  subject "First multipart email sent with Mail"

  text_part do
    body "This is plain text"
  end

  html_part do
    content_type "text/html; charset=UTF-8"
    body "<h1>This is HTML</h1>"
  end

  add_file "./e2e/spec/fixtures/attachment.txt"
  add_file "./e2e/spec/fixtures/image1.jpg"
end

# Net::SMTP.start("localhost", 3025) { |smtp| smtp.send_message mail.to_s, FROM_ADDRESS, TO }
Net::SMTP.start("localhost", 1025) { |smtp| smtp.send_message mail.to_s, FROM_ADDRESS, TO }
