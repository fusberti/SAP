################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../src/inputoutput/read.cpp \
../src/inputoutput/readinstance.cpp \
../src/inputoutput/write.cpp \
../src/inputoutput/writearcs.cpp \
../src/inputoutput/writeplotfile.cpp 

OBJS += \
./src/inputoutput/read.o \
./src/inputoutput/readinstance.o \
./src/inputoutput/write.o \
./src/inputoutput/writearcs.o \
./src/inputoutput/writeplotfile.o 

CPP_DEPS += \
./src/inputoutput/read.d \
./src/inputoutput/readinstance.d \
./src/inputoutput/write.d \
./src/inputoutput/writearcs.d \
./src/inputoutput/writeplotfile.d 


# Each subdirectory must supply rules for building sources it contributes
src/inputoutput/%.o: ../src/inputoutput/%.cpp src/inputoutput/subdir.mk
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -O3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$@" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


