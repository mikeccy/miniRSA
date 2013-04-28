build: clean
	@echo "\nNote: Works best in Java SE 6 (v1.6)\n"
	@echo "\n1. Make output directory"
	mkdir bin
	@echo "\n2. Compile in target version 1.6"
	javac -source 1.6 -target 1.6 -d bin src/edu/cit595/qyccy/exception/InvalidDataFormatException.java src/edu/cit595/qyccy/exception/InvalidKeyException.java src/edu/cit595/qyccy/exception/InvalidHeaderException.java src/edu/cit595/qyccy/model/Request.java src/edu/cit595/qyccy/model/Respond.java src/edu/cit595/qyccy/client/ClientGui.java src/edu/cit595/qyccy/client/Client.java src/edu/cit595/qyccy/client/JListExtended.java src/edu/cit595/qyccy/client/PopupMenu.java src/edu/cit595/qyccy/common/Configs.java src/edu/cit595/qyccy/model/Header.java src/edu/cit595/qyccy/model/Message.java src/edu/cit595/qyccy/server/ClientStatus.java src/edu/cit595/qyccy/server/ServerConnection.java src/edu/cit595/qyccy/server/Server.java src/edu/cit595/qyccy/transfer/Connection.java src/edu/cit595/qyccy/transfer/Encryption.java src/edu/cit595/qyccy/transfer/Protocol.java src/edu/cit595/qyccy/transfer/RSA.java
	@echo "\n3. Copy config file to bin"
	cp -r configs bin

run: build run_server

run_server:
	@echo "\nRunning server..."
	(cd bin/ && java edu.cit595.qyccy.server.Server)

run_client:
	@echo "\nRunning client..."
	(cd bin/ && java edu.cit595.qyccy.client.Client)

clean:
	@echo "\nCleaning project..."
	rm -rf bin/
