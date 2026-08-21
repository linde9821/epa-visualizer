# Reverted to normal FROM to clear the warning
FROM kasmweb/core-ubuntu-jammy:1.15.0

# Switch to root to install packages and modify files
USER root

# Install unzip
RUN apt-get update \
    && apt-get install -y unzip \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy your .zip packages into the temporary container folder
COPY epa-visualizer.zip /tmp/epa-visualizer.zip

# FIX: Use JSON array syntax to copy files with spaces!
COPY ["User Study Project.zip", "/tmp/User Study Project.zip"]

# Unzip the application into /opt/ and remove the zip file
RUN unzip /tmp/epa-visualizer.zip -d /opt/ \
    && rm /tmp/epa-visualizer.zip

# Unzip the Project directly to the Kasm default desktop
RUN unzip "/tmp/User Study Project.zip" -d "/home/kasm-default-profile/Desktop/" \
    && rm "/tmp/User Study Project.zip"

# Give the Kasm user permission to actually use and edit the project files
RUN chown -R 1000:1000 "/home/kasm-default-profile/Desktop/"

# Make the launch script executable
RUN chmod +x "/opt/EPA Visualizer/bin/EPA Visualizer"

# Copy the custom startup script and set permissions
COPY custom_startup.sh $STARTUPDIR/custom_startup.sh
RUN chmod +x $STARTUPDIR/custom_startup.sh \
    && chown 1000:1000 $STARTUPDIR/custom_startup.sh

# Switch back to the default Kasm non-root user
USER 1000